package com.tamdao.taixiu_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_code", unique = true, nullable = false, length = 20)
    private String sessionCode;
    
    private Integer dice1;
    private Integer dice2;
    private Integer dice3;
    private Integer total;
    
    @Enumerated(EnumType.STRING)
    private GameResult result;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameStatus status = GameStatus.BETTING;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bet> bets = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
    }
    
    public enum GameResult {
        TAI, XIU
    }
    
    public enum GameStatus {
        BETTING,    // Đang đặt cược
        SPINNING,   // Đang quay xúc xắc
        COMPLETED   // Đã hoàn thành
    }
}
