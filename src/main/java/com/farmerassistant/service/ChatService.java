package com.farmerassistant.service;

import com.farmerassistant.dto.request.ChatRequest;
import com.farmerassistant.dto.response.ChatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for the AI farming chatbot.
 */
public interface ChatService {

    /**
     * Process a farming question and return an AI-generated answer.
     *
     * @param request the chat request with the question
     * @param userEmail the authenticated farmer's email
     * @return ChatResponse with AI answer and metadata
     */
    ChatResponse askQuestion(ChatRequest request, String userEmail);

    /**
     * Retrieve paginated chat history for a user.
     *
     * @param userEmail the authenticated farmer's email
     * @param pageable pagination params
     * @return page of past Q&A records
     */
    Page<ChatResponse> getChatHistory(String userEmail, Pageable pageable);
}
