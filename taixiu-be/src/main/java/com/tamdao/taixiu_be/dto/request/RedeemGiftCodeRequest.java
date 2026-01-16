package com.tamdao.taixiu_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RedeemGiftCodeRequest {
    
    @NotBlank(message = "Code is required")
    private String code;
}
