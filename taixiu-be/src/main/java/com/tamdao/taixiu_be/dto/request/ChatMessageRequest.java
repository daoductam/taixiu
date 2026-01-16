package com.tamdao.taixiu_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequest {
    
    @NotBlank(message = "Message content is required")
    private String content;
}
