package com.farmerassistant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO containing the AI-analyzed crop disease detection result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseDetectionResponse {

    private Long logId;
    private String imageName;
    private String diseaseName;
    private String confidenceLevel;
    private String cure;
    private String preventionTips;
    private String status;
    private LocalDateTime detectedAt;
    private String message;
}
