package com.farmerassistant.service;

import com.farmerassistant.dto.response.DiseaseDetectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for crop disease detection.
 */
public interface DiseaseDetectionService {

    /**
     * Analyze a crop image for diseases using OpenAI Vision.
     *
     * @param image the uploaded image file
     * @param userEmail the authenticated farmer's email
     * @return structured disease detection result
     */
    DiseaseDetectionResponse detectDisease(MultipartFile image, String userEmail);

    /**
     * Retrieve paginated disease detection history for a user.
     *
     * @param userEmail the authenticated farmer's email
     * @param pageable pagination params
     * @return page of past detections
     */
    Page<DiseaseDetectionResponse> getUserHistory(String userEmail, Pageable pageable);
}
