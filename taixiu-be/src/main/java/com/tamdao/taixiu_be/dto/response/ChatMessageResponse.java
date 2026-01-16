package com.tamdao.taixiu_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String username;
    private String avatarUrl;
    private String content;
    private Boolean isAi;
    private LocalDateTime createdAt;
}
