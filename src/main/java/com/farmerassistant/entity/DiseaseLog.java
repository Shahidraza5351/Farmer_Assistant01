package com.farmerassistant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity to store crop disease detection results.
 * Each record represents one AI analysis of an uploaded crop image.
 */
@Entity
@Table(name = "disease_logs", indexes = {
    @Index(name = "idx_disease_user_id", columnList = "user_id"),
    @Index(name = "idx_disease_detected_at", columnList = "detected_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    @Column(name = "image_content_type", length = 50)
    private String imageContentType;

    @Column(name = "disease_name", length = 200)
    private String diseaseName;

    @Column(name = "confidence_level", length = 50)
    private String confidenceLevel;

    @Column(name = "cure", columnDefinition = "TEXT")
    private String cure;

    @Column(name = "prevention_tips", columnDefinition = "TEXT")
    private String preventionTips;

    @Column(name = "raw_ai_response", columnDefinition = "TEXT")
    private String rawAiResponse;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @CreationTimestamp
    @Column(name = "detected_at", updatable = false)
    private LocalDateTime detectedAt;
}
