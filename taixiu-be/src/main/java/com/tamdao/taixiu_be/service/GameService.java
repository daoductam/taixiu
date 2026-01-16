package com.tamdao.taixiu_be.service;

import com.tamdao.taixiu_be.dto.response.BetResponse;
import com.tamdao.taixiu_be.dto.response.GameSessionResponse;
import com.tamdao.taixiu_be.entity.*;
import com.tamdao.taixiu_be.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    
    private final GameSessionRepository gameSessionRepository;
    private final BetRepository betRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${game.round-duration:30}")
    private int roundDuration;
    
    @Value("${game.betting-duration:20}")
    private int bettingDuration;
    
    @Value("${game.payout-ratio:1.0}")
    private double payoutRatio;
    
    public GameSessionResponse getCurrentGame() {
        GameSession game = gameSessionRepository.findCurrentGame()
                .orElse(null);
        
        if (game == null) {
            return null;
        }
        
        return buildGameResponse(game);
    }
    
    @Transactional
    public GameSession createNewGame() {
        String sessionCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        GameSession game = GameSession.builder()
                .sessionCode(sessionCode)
                .status(GameSession.GameStatus.BETTING)
                .build();
        
        game = gameSessionRepository.save(game);
        log.info("Created new game session: {}", sessionCode);
        
        return game;
    }
    
    @Transactional
    public BetResponse placeBet(String username, String betType, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        GameSession game = gameSessionRepository.findCurrentGame()
                .orElseThrow(() -> new RuntimeException("No active game session"));
        
        if (game.getStatus() != GameSession.GameStatus.BETTING) {
            throw new RuntimeException("Betting is closed for this round");
        }
        
        // Check remaining time
        long secondsElapsed = ChronoUnit.SECONDS.between(game.getStartTime(), LocalDateTime.now());
        if (secondsElapsed >= bettingDuration) {
            throw new RuntimeException("Betting time has ended");
        }
        
        // Check balance
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        // Parse bet type
        Bet.BetType type;
        try {
            type = Bet.BetType.valueOf(betType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid bet type. Use TAI or XIU");
        }
        
        // Check if user already bet on the opposite type this round
        List<Bet> existingBets = betRepository.findByUserIdAndGameSessionId(user.getId(), game.getId());
        for (Bet existingBet : existingBets) {
            if (existingBet.getBetType() != type) {
                throw new RuntimeException("Bạn đã đặt " + existingBet.getBetType().name() + 
                    " trong vòng này. Không thể đặt cả TÀI và XỈU!");
            }
        }
        
        // Deduct balance
        BigDecimal balanceBefore = user.getBalance();
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        
        // Create bet
        Bet bet = Bet.builder()
                .user(user)
                .gameSession(game)
                .betType(type)
                .amount(amount)
                .build();
        bet = betRepository.save(bet);
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(Transaction.TransactionType.BET)
                .amount(amount.negate())
                .balanceBefore(balanceBefore)
                .balanceAfter(user.getBalance())
                .description("Bet " + type + " on game " + game.getSessionCode())
                .refId(bet.getId())
                .build();
        transactionRepository.save(transaction);
        
        log.info("User {} placed bet: {} {} on game {}", username, amount, type, game.getSessionCode());
        
        // Broadcast updated game state
        broadcastGameUpdate(game);
        
        return BetResponse.builder()
                .id(bet.getId())
                .betType(bet.getBetType().name())
                .amount(bet.getAmount())
                .createdAt(bet.getCreatedAt())
                .sessionCode(game.getSessionCode())
                .build();
    }
    
    @Transactional
    public void closebetting(Long gameId) {
        GameSession game = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        game.setStatus(GameSession.GameStatus.SPINNING);
        gameSessionRepository.save(game);
        
        log.info("Betting closed for game: {}", game.getSessionCode());
        broadcastGameUpdate(game);
    }
    
    @Transactional
    public void spinDice(Long gameId) {
        GameSession game = gameSessionRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        // Roll 3 dice
        int dice1 = secureRandom.nextInt(6) + 1;
        int dice2 = secureRandom.nextInt(6) + 1;
        int dice3 = secureRandom.nextInt(6) + 1;
        int total = dice1 + dice2 + dice3;
        
        // Determine result (3-10 = XIU, 11-18 = TAI)
        GameSession.GameResult result = total <= 10 ? GameSession.GameResult.XIU : GameSession.GameResult.TAI;
        
        game.setDice1(dice1);
        game.setDice2(dice2);
        game.setDice3(dice3);
        game.setTotal(total);
        game.setResult(result);
        game.setStatus(GameSession.GameStatus.COMPLETED);
        game.setEndTime(LocalDateTime.now());
        
        gameSessionRepository.save(game);
        
        log.info("Game {} result: {} {} {} = {} ({})", 
                game.getSessionCode(), dice1, dice2, dice3, total, result);
        
        // Calculate winnings
        calculateWinnings(game);
        
        // Broadcast result
        broadcastGameUpdate(game);
    }
    
    @Transactional
    public void calculateWinnings(GameSession game) {
        List<Bet> bets = betRepository.findByGameSessionId(game.getId());
        
        for (Bet bet : bets) {
            boolean isWin = bet.getBetType().name().equals(game.getResult().name());
            bet.setIsWin(isWin);
            
            if (isWin) {
                BigDecimal winAmount = bet.getAmount().multiply(BigDecimal.valueOf(payoutRatio));
                bet.setWinAmount(winAmount);
                
                User user = bet.getUser();
                BigDecimal balanceBefore = user.getBalance();
                // Return original bet + winnings
                BigDecimal totalWin = bet.getAmount().add(winAmount);
                user.setBalance(user.getBalance().add(totalWin));
                userRepository.save(user);
                
                // Create win transaction
                Transaction transaction = Transaction.builder()
                        .user(user)
                        .type(Transaction.TransactionType.WIN)
                        .amount(totalWin)
                        .balanceBefore(balanceBefore)
                        .balanceAfter(user.getBalance())
                        .description("Won bet on game " + game.getSessionCode())
                        .refId(bet.getId())
                        .build();
                transactionRepository.save(transaction);
                
                log.info("User {} won {} from game {}", user.getUsername(), totalWin, game.getSessionCode());
            }
            
            betRepository.save(bet);
        }
    }
    
    public List<GameSessionResponse.GameHistoryItem> getGameHistory() {
        return gameSessionRepository.findLast10CompletedGames()
                .stream()
                .map(g -> GameSessionResponse.GameHistoryItem.builder()
                        .id(g.getId())
                        .sessionCode(g.getSessionCode())
                        .total(g.getTotal())
                        .result(g.getResult() != null ? g.getResult().name() : null)
                        .endTime(g.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }
    
    public void broadcastGameUpdate(GameSession game) {
        GameSessionResponse response = buildGameResponse(game);
        messagingTemplate.convertAndSend("/topic/game", response);
    }
    
    public void broadcastUserBalance(String username, BigDecimal balance) {
        messagingTemplate.convertAndSendToUser(username, "/queue/balance", balance);
    }
    
    private GameSessionResponse buildGameResponse(GameSession game) {
        BigDecimal totalTai = betRepository.getTotalBetAmount(game.getId(), Bet.BetType.TAI);
        BigDecimal totalXiu = betRepository.getTotalBetAmount(game.getId(), Bet.BetType.XIU);
        Long countTai = betRepository.countBetsByType(game.getId(), Bet.BetType.TAI);
        Long countXiu = betRepository.countBetsByType(game.getId(), Bet.BetType.XIU);
        
        // Calculate remaining seconds
        Integer remainingSeconds = null;
        if (game.getStatus() == GameSession.GameStatus.BETTING) {
            long elapsed = ChronoUnit.SECONDS.between(game.getStartTime(), LocalDateTime.now());
            remainingSeconds = Math.max(0, bettingDuration - (int) elapsed);
        } else if (game.getStatus() == GameSession.GameStatus.SPINNING) {
            long elapsed = ChronoUnit.SECONDS.between(game.getStartTime(), LocalDateTime.now());
            remainingSeconds = Math.max(0, roundDuration - (int) elapsed);
        }
        
        return GameSessionResponse.builder()
                .id(game.getId())
                .sessionCode(game.getSessionCode())
                .dice1(game.getDice1())
                .dice2(game.getDice2())
                .dice3(game.getDice3())
                .total(game.getTotal())
                .result(game.getResult() != null ? game.getResult().name() : null)
                .status(game.getStatus().name())
                .startTime(game.getStartTime())
                .endTime(game.getEndTime())
                .totalBetTai(totalTai)
                .totalBetXiu(totalXiu)
                .countBetTai(countTai)
                .countBetXiu(countXiu)
                .remainingSeconds(remainingSeconds)
                .history(getGameHistory())
                .build();
    }
}
