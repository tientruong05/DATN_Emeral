package com.poly.dto;

import java.util.Objects;

public record ChatbotDTO(String question, String answer) {
    public ChatbotDTO {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Câu hỏi không được để trống");
        }
    }
} 