package com.tamdao.taixiu_be.controller;

import com.tamdao.taixiu_be.dto.request.ChatMessageRequest;
import com.tamdao.taixiu_be.dto.response.ApiResponse;
import com.tamdao.taixiu_be.dto.response.ChatMessageResponse;
import com.tamdao.taixiu_be.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getRecentMessages() {
        List<ChatMessageResponse> messages = chatService.getRecentMessages();
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(userDetails.getUsername(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // WebSocket message handler
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal != null) {
            chatService.sendMessage(principal.getName(), request.getContent());
        }
    }
}
