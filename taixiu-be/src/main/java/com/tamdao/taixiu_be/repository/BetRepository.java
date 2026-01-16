package com.tamdao.taixiu_be.repository;

import com.tamdao.taixiu_be.entity.Bet;
import com.tamdao.taixiu_be.entity.Bet.BetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    
    List<Bet> findByGameSessionId(Long gameSessionId);
    
    List<Bet> findByUserId(Long userId);
    
    Page<Bet> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Bet> findByGameSessionIdAndBetType(Long gameSessionId, BetType betType);
    
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bet b WHERE b.gameSession.id = :gameSessionId AND b.betType = :betType")
    BigDecimal getTotalBetAmount(@Param("gameSessionId") Long gameSessionId, @Param("betType") BetType betType);
    
    @Query("SELECT COUNT(b) FROM Bet b WHERE b.gameSession.id = :gameSessionId AND b.betType = :betType")
    Long countBetsByType(@Param("gameSessionId") Long gameSessionId, @Param("betType") BetType betType);
    
    boolean existsByUserIdAndGameSessionId(Long userId, Long gameSessionId);
    
    List<Bet> findByUserIdAndGameSessionId(Long userId, Long gameSessionId);
}
