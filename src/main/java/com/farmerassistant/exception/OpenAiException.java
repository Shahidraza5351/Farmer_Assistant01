package com.farmerassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when the OpenAI API call fails or returns an error. */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class OpenAiException extends RuntimeException {
    public OpenAiException(String message) {
        super(message);
    }

    public OpenAiException(String message, Throwable cause) {
        super(message, cause);
    }
}
