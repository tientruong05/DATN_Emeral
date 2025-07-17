package com.poly.controller;

import com.poly.dto.ApiResponse;
import com.poly.dto.ChatbotDTO;
import com.poly.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatbotDTO>> ask(@RequestBody ChatbotDTO request) {
        try {
            ChatbotDTO response = chatbotService.askProgrammingQuestion(request.question());
            return ResponseEntity.ok(new ApiResponse<>(true, "Success", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
} 