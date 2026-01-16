package com.tamdao.taixiu_be.service;

import com.tamdao.taixiu_be.dto.response.ChatMessageResponse;
import com.tamdao.taixiu_be.entity.ChatMessage;
import com.tamdao.taixiu_be.entity.User;
import com.tamdao.taixiu_be.repository.ChatMessageRepository;
import com.tamdao.taixiu_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public ChatMessageResponse sendMessage(String username, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Save user message
        ChatMessage message = ChatMessage.builder()
                .user(user)
                .content(content)
                .isAi(false)
                .build();
        message = chatMessageRepository.save(message);
        
        ChatMessageResponse response = mapToResponse(message, user);
        
        // Broadcast to all users
        messagingTemplate.convertAndSend("/topic/chat", response);
        
        // Check if message is directed to AI (starts with @bot or @ai)
        if (content.toLowerCase().startsWith("@bot") || content.toLowerCase().startsWith("@ai")) {
            String aiQuery = content.replaceFirst("(?i)@(bot|ai)\\s*", "");
            handleAiResponse(aiQuery);
        }
        
        return response;
    }
    
    private void handleAiResponse(String query) {
        String aiResponse = aiService.chat(query);
        
        // Save AI message
        ChatMessage aiMessage = ChatMessage.builder()
                .content(aiResponse)
                .isAi(true)
                .build();
        aiMessage = chatMessageRepository.save(aiMessage);
        
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(aiMessage.getId())
                .username("🤖 AI Assistant")
                .content(aiMessage.getContent())
                .isAi(true)
                .createdAt(aiMessage.getCreatedAt())
                .build();
        
        // Broadcast AI response
        messagingTemplate.convertAndSend("/topic/chat", response);
    }
    
    public List<ChatMessageResponse> getRecentMessages() {
        return chatMessageRepository.findLast50Messages()
                .stream()
                .map(msg -> {
                    if (msg.getIsAi()) {
                        return ChatMessageResponse.builder()
                                .id(msg.getId())
                                .username("🤖 AI Assistant")
                                .content(msg.getContent())
                                .isAi(true)
                                .createdAt(msg.getCreatedAt())
                                .build();
                    } else {
                        return mapToResponse(msg, msg.getUser());
                    }
                })
                .collect(Collectors.toList());
    }
    
    private ChatMessageResponse mapToResponse(ChatMessage message, User user) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .content(message.getContent())
                .isAi(false)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
