package com.tamdao.taixiu_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionResponse {
    private Long id;
    private String sessionCode;
    private Integer dice1;
    private Integer dice2;
    private Integer dice3;
    private Integer total;
    private String result;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalBetTai;
    private BigDecimal totalBetXiu;
    private Long countBetTai;
    private Long countBetXiu;
    private Integer remainingSeconds;
    private List<GameHistoryItem> history;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameHistoryItem {
        private Long id;
        private String sessionCode;
        private Integer total;
        private String result;
        private LocalDateTime endTime;
    }
}
