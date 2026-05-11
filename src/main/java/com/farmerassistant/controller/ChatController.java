package com.farmerassistant.controller;

import com.farmerassistant.dto.request.ChatRequest;
import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.ChatResponse;
import com.farmerassistant.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the AI farming chatbot.
 *
 * All endpoints require JWT authentication.
 *
 *   POST /api/chat         → Ask a farming question
 *   GET  /api/chat/history → Get past Q&A history (paginated)
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * Ask the AI chatbot a farming question.
     *
     * Requires: JWT Bearer token in Authorization header
     *
     * @param request contains the farmer's question
     * @param userDetails injected from JWT context
     * @return AI-generated farmer-friendly answer
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> askQuestion(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Chat question from: {}", userDetails.getUsername());

        ChatResponse response = chatService.askQuestion(request, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(
                "AI response generated successfully",
                response
        ));
    }

    /**
     * Retrieve the authenticated farmer's chat history.
     *
     * @param page page number (0-indexed, default 0)
     * @param size results per page (default 10, max 50)
     * @param userDetails injected from JWT context
     * @return paginated chat history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<ChatResponse>>> getChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<ChatResponse> history = chatService.getChatHistory(
                userDetails.getUsername(),
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Chat history retrieved",
                history
        ));
    }
}
