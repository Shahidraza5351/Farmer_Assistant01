package com.farmerassistant.service;

import com.farmerassistant.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-bucket rate limiter for Chat and Disease Detection endpoints.
 *
 * Each user gets their own bucket per endpoint type.
 * Bucket refills gradually over the configured time window.
 */
@Service
@Slf4j
public class RateLimiterService {

    // Chat rate limit config
    @Value("${app.rate-limit.chat.capacity}")
    private int chatCapacity;

    @Value("${app.rate-limit.chat.refill-tokens}")
    private int chatRefillTokens;

    @Value("${app.rate-limit.chat.refill-seconds}")
    private int chatRefillSeconds;

    // Disease detection rate limit config
    @Value("${app.rate-limit.disease.capacity}")
    private int diseaseCapacity;

    @Value("${app.rate-limit.disease.refill-tokens}")
    private int diseaseRefillTokens;

    @Value("${app.rate-limit.disease.refill-seconds}")
    private int diseaseRefillSeconds;

    // Per-user per-endpoint buckets
    private final Map<String, Bucket> chatBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> diseaseBuckets = new ConcurrentHashMap<>();

    /**
     * Try to consume a token from the CHAT rate limit bucket for the given user.
     * Throws RateLimitException if limit is exceeded.
     *
     * @param userEmail the authenticated user's email
     */
    public void checkChatRateLimit(String userEmail) {
        Bucket bucket = chatBuckets.computeIfAbsent(userEmail, k -> createChatBucket());
        if (!bucket.tryConsume(1)) {
            log.warn("Chat rate limit exceeded for user: {}", userEmail);
            throw new RateLimitException(
                "You have exceeded the chat limit. Please wait before sending another message. " +
                "Limit: " + chatCapacity + " requests per " + chatRefillSeconds + " seconds."
            );
        }
    }

    /**
     * Try to consume a token from the DISEASE DETECTION rate limit bucket for the given user.
     * Throws RateLimitException if limit is exceeded.
     *
     * @param userEmail the authenticated user's email
     */
    public void checkDiseaseRateLimit(String userEmail) {
        Bucket bucket = diseaseBuckets.computeIfAbsent(userEmail, k -> createDiseaseBucket());
        if (!bucket.tryConsume(1)) {
            log.warn("Disease detection rate limit exceeded for user: {}", userEmail);
            throw new RateLimitException(
                "You have exceeded the disease detection limit. Please wait before uploading another image. " +
                "Limit: " + diseaseCapacity + " requests per " + diseaseRefillSeconds + " seconds."
            );
        }
    }

    /** Build a rate-limit bucket for the chat endpoint. */
    private Bucket createChatBucket() {
        Bandwidth limit = Bandwidth.classic(
            chatCapacity,
            Refill.greedy(chatRefillTokens, Duration.ofSeconds(chatRefillSeconds))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /** Build a rate-limit bucket for the disease detection endpoint. */
    private Bucket createDiseaseBucket() {
        Bandwidth limit = Bandwidth.classic(
            diseaseCapacity,
            Refill.greedy(diseaseRefillTokens, Duration.ofSeconds(diseaseRefillSeconds))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
