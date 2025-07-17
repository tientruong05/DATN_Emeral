package com.poly.service;

import com.poly.dto.ChatbotDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import javax.annotation.PostConstruct;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.endpoint}")
    private String geminiApiEndpoint;

    private WebClient webClient;

    @PostConstruct
    public void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl(geminiApiEndpoint)
                .build();
    }

    @Override
    public ChatbotDTO askProgrammingQuestion(String question) {
        // Simple filter: only allow programming-related questions
        if (!isProgrammingQuestion(question)) {
            return new ChatbotDTO(question, "Xin lỗi, bạn chỉ được hỏi về các chủ đề lập trình.");
        }
        // Thêm prompt hướng dẫn trả lời ngắn gọn
        String shortPrompt = "Hãy trả lời ngắn gọn, tối đa 500 đến 600 ký tự: " + question;
        String answer = callGeminiApi(shortPrompt);
        // Nếu trả lời quá dài, cắt còn 500 ký tự
        if (answer != null && answer.length() > 500) {
            answer = answer.substring(0, 1000) + "...";
        }
        return new ChatbotDTO(question, answer);
    }

    private boolean isProgrammingQuestion(String question) {
        String lower = question.toLowerCase();
        return lower.contains("lập trình") || lower.contains("code") || lower.contains("java") ||
               lower.contains("python") || lower.contains("c++") || lower.contains("c#") ||
               lower.contains("javascript") || lower.contains("spring") || lower.contains("backend") ||
               lower.contains("frontend") || lower.contains("database") || lower.contains("sql") ||
               lower.contains("algorithm") || lower.contains("thuật toán") || lower.contains("dev");
    }

    private String callGeminiApi(String question) {
        try {
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + question.replace("\"", "\\\"") + "\"}]}]}";
            String url = geminiApiEndpoint + "?key=" + geminiApiKey;
            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            // Parse JSON để lấy đầy đủ answer
            if (response != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        String text = parts.get(0).path("text").asText();
                        if (text != null && !text.isEmpty()) {
                            return text;
                        }
                    }
                }
            }
            return "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.";
        } catch (Exception e) {
            return "Đã xảy ra lỗi khi kết nối với AI: " + e.getMessage();
        }
    }
} 