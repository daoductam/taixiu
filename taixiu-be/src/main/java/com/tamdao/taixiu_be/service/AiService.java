package com.tamdao.taixiu_be.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
    
    @Value("${gemini.api-key:}")
    private String apiKey;
    
    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String apiUrl;
    
    private final WebClient webClient = WebClient.builder().build();
    
    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI của game Tài Xỉu. Nhiệm vụ của bạn:
        1. Giải thích luật chơi Tài Xỉu (3 xúc xắc, tổng 3-10 là Xỉu, 11-18 là Tài)
        2. Trả lời câu hỏi về game một cách thân thiện
        3. KHÔNG dự đoán kết quả vì game hoàn toàn ngẫu nhiên
        4. Khuyến khích chơi có trách nhiệm với tiền ảo
        5. Trả lời ngắn gọn, tối đa 2-3 câu
        6. Luôn trả lời bằng tiếng Việt
        """;
    
    public String chat(String userMessage) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            return getDefaultResponse(userMessage);
        }
        
        try {
            String fullUrl = apiUrl + "?key=" + apiKey;
            
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", SYSTEM_PROMPT + "\n\nUser: " + userMessage)
                    ))
                ),
                "generationConfig", Map.of(
                    "maxOutputTokens", 150,
                    "temperature", 0.7
                )
            );
            
            Map<String, Object> response = webClient.post()
                    .uri(fullUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            
            return getDefaultResponse(userMessage);
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return getDefaultResponse(userMessage);
        }
    }
    
    private String getDefaultResponse(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("luật") || lowerMessage.contains("chơi") || lowerMessage.contains("cách")) {
            return "🎲 Luật chơi Tài Xỉu: Quay 3 xúc xắc, tổng 3-10 là Xỉu, tổng 11-18 là Tài. Đặt cược đúng thì thắng gấp đôi!";
        }
        
        if (lowerMessage.contains("dự đoán") || lowerMessage.contains("kết quả")) {
            return "🎰 Xúc xắc hoàn toàn ngẫu nhiên nên mình không thể dự đoán được. Chúc bạn may mắn!";
        }
        
        if (lowerMessage.contains("nạp") || lowerMessage.contains("tiền") || lowerMessage.contains("code")) {
            return "💰 Bạn có thể nhận tiền ảo bằng cách nhập Gift Code từ Admin. Game chỉ sử dụng tiền ảo để giải trí!";
        }
        
        return "🎲 Xin chào! Tôi là trợ lý AI của game Tài Xỉu. Bạn cần hỗ trợ gì về luật chơi hoặc cách chơi không?";
    }
}
