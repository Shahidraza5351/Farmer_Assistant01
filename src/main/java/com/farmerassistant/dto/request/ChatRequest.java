package com.farmerassistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for AI chatbot query request.
 */
@Data
public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(min = 3, max = 1000, message = "Question must be between 3 and 1000 characters")
    private String question;
}
