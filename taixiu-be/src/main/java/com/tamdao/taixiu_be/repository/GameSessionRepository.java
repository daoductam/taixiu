package com.tamdao.taixiu_be.repository;

import com.tamdao.taixiu_be.entity.GameSession;
import com.tamdao.taixiu_be.entity.GameSession.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    Optional<GameSession> findBySessionCode(String sessionCode);
    
    Optional<GameSession> findFirstByStatusOrderByStartTimeDesc(GameStatus status);
    
    @Query("SELECT g FROM GameSession g WHERE g.status != 'COMPLETED' ORDER BY g.startTime DESC LIMIT 1")
    Optional<GameSession> findCurrentGame();
    
    @Query("SELECT g FROM GameSession g WHERE g.status = 'COMPLETED' ORDER BY g.endTime DESC LIMIT 10")
    List<GameSession> findLast10CompletedGames();
    
    @Query("SELECT g FROM GameSession g ORDER BY g.startTime DESC LIMIT 1")
    Optional<GameSession> findLatestGame();
}
