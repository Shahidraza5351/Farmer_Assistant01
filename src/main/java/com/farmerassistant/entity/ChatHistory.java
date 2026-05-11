package com.farmerassistant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity to persist the conversation history between farmers and the AI chatbot.
 */
@Entity
@Table(name = "chat_history", indexes = {
    @Index(name = "idx_chat_user_id", columnList = "user_id"),
    @Index(name = "idx_chat_asked_at", columnList = "asked_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @CreationTimestamp
    @Column(name = "asked_at", updatable = false)
    private LocalDateTime askedAt;
}
