package com.farmerassistant.service.impl;

import com.farmerassistant.dto.request.ChatRequest;
import com.farmerassistant.dto.response.ChatResponse;
import com.farmerassistant.entity.ChatHistory;
import com.farmerassistant.entity.User;
import com.farmerassistant.exception.ResourceNotFoundException;
import com.farmerassistant.repository.ChatHistoryRepository;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.ChatService;
import com.farmerassistant.service.OpenAiService;
import com.farmerassistant.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ChatService.
 *
 * Flow:
 * 1. Apply per-user rate limit
 * 2. Load user from DB
 * 3. Call OpenAI Chat API
 * 4. Persist Q&A to chat_history table
 * 5. Return DTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final OpenAiService openAiService;
    private final RateLimiterService rateLimiterService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatResponse askQuestion(ChatRequest request, String userEmail) {
        log.info("Chat question from: {}", userEmail);

        // 1. Rate limiting check
        rateLimiterService.checkChatRateLimit(userEmail);

        // 2. Load user
        User user = userRepository.findByEmailAndIsActiveTrue(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // 3. Time the OpenAI call
        long startTime = System.currentTimeMillis();
        String answer = openAiService.askFarmerQuery(request.getQuestion());
        long responseTimeMs = System.currentTimeMillis() - startTime;

        log.info("Chat AI responded in {}ms for user: {}", responseTimeMs, userEmail);

        // 4. Persist to chat_history
        ChatHistory chatHistory = ChatHistory.builder()
                .user(user)
                .question(request.getQuestion())
                .answer(answer)
                .responseTimeMs(responseTimeMs)
                .status("SUCCESS")
                .build();

        chatHistory = chatHistoryRepository.save(chatHistory);

        // 5. Return DTO
        return ChatResponse.builder()
                .chatId(chatHistory.getId())
                .question(request.getQuestion())
                .answer(answer)
                .responseTimeMs(responseTimeMs)
                .askedAt(chatHistory.getAskedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatResponse> getChatHistory(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmailAndIsActiveTrue(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return chatHistoryRepository
                .findByUserIdOrderByAskedAtDesc(user.getId(), pageable)
                .map(history -> ChatResponse.builder()
                        .chatId(history.getId())
                        .question(history.getQuestion())
                        .answer(history.getAnswer())
                        .tokensUsed(history.getTokensUsed())
                        .responseTimeMs(history.getResponseTimeMs())
                        .askedAt(history.getAskedAt())
                        .build()
                );
    }
}
