package com.tamdao.taixiu_be.service;

import com.tamdao.taixiu_be.entity.GameSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class GameSchedulerService {
    
    private final GameService gameService;
    
    @Value("${game.round-duration:30}")
    private int roundDuration;
    
    @Value("${game.betting-duration:20}")
    private int bettingDuration;
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private GameSession currentGame;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready, starting game loop...");
        startNewRound();
    }
    
    private void startNewRound() {
        try {
            currentGame = gameService.createNewGame();
            gameService.broadcastGameUpdate(currentGame);
            
            // Schedule betting close
            scheduler.schedule(() -> closeBetting(), bettingDuration, TimeUnit.SECONDS);
            
            log.info("New round started: {}, betting closes in {} seconds", 
                    currentGame.getSessionCode(), bettingDuration);
        } catch (Exception e) {
            log.error("Error starting new round", e);
            // Retry after 5 seconds
            scheduler.schedule(this::startNewRound, 5, TimeUnit.SECONDS);
        }
    }
    
    private void closeBetting() {
        try {
            if (currentGame != null) {
                gameService.closebetting(currentGame.getId());
                
                // Schedule dice spin after a short delay
                int spinDelay = roundDuration - bettingDuration;
                scheduler.schedule(() -> spinAndNextRound(), spinDelay, TimeUnit.SECONDS);
                
                log.info("Betting closed for game {}, spinning in {} seconds", 
                        currentGame.getSessionCode(), spinDelay);
            }
        } catch (Exception e) {
            log.error("Error closing betting", e);
        }
    }
    
    private void spinAndNextRound() {
        try {
            if (currentGame != null) {
                gameService.spinDice(currentGame.getId());
                log.info("Dice spun for game {}", currentGame.getSessionCode());
            }
            
            // Start next round after a short delay to show results
            scheduler.schedule(this::startNewRound, 3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error spinning dice", e);
            // Start next round anyway
            scheduler.schedule(this::startNewRound, 5, TimeUnit.SECONDS);
        }
    }
}
