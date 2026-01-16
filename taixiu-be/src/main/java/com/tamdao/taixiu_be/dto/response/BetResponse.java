package com.tamdao.taixiu_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetResponse {
    private Long id;
    private String betType;
    private BigDecimal amount;
    private BigDecimal winAmount;
    private Boolean isWin;
    private LocalDateTime createdAt;
    private String sessionCode;
}
