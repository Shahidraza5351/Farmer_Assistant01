package com.farmerassistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the WebClient used to communicate with the OpenAI API.
 * Sets default headers and base URL.
 */
@Configuration
public class WebClientConfig {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.base-url}")
    private String openAiBaseUrl;

    /**
     * WebClient bean configured for OpenAI API.
     * - Authorization header with Bearer token
     * - 16MB buffer for image payloads
     */
    @Bean
    public WebClient openAiWebClient() {
        // Increase in-memory buffer to handle large base64 image payloads
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024) // 16MB
                )
                .build();

        return WebClient.builder()
                .baseUrl(openAiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .exchangeStrategies(strategies)
                .build();
    }
}
