package com.tamdao.taixiu_be.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceBetRequest {
    
    @NotBlank(message = "Bet type is required (TAI or XIU)")
    private String betType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum bet is 1000")
    private BigDecimal amount;
}
