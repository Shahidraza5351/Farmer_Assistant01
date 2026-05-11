package com.farmerassistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO containing the AI chatbot's response to a farmer's question.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long chatId;
    private String question;
    private String answer;
    private Integer tokensUsed;
    private Long responseTimeMs;
    private LocalDateTime askedAt;
}
