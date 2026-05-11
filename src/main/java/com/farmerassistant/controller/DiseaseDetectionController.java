package com.farmerassistant.controller;

import com.farmerassistant.dto.response.ApiResponse;
import com.farmerassistant.dto.response.DiseaseDetectionResponse;
import com.farmerassistant.service.DiseaseDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for crop disease detection.
 *
 * All endpoints require JWT authentication.
 *
 *   POST /api/disease/detect      → Upload crop image and detect disease
 *   GET  /api/disease/history     → Retrieve past detections (paginated)
 */
@RestController
@RequestMapping("/api/disease")
@RequiredArgsConstructor
@Slf4j
public class DiseaseDetectionController {

    private final DiseaseDetectionService diseaseDetectionService;

    /**
     * Analyze a crop image for disease.
     *
     * Accepts: multipart/form-data with field "image"
     * Requires: JWT Bearer token in Authorization header
     *
     * @param image the crop image file (JPEG, PNG, WebP — max 10MB)
     * @param userDetails injected from JWT context
     * @return disease name, confidence, cure, and prevention tips
     */
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiseaseDetectionResponse>> detectDisease(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Disease detection requested by user: {}, file: {}",
                userDetails.getUsername(), image.getOriginalFilename());

        DiseaseDetectionResponse response = diseaseDetectionService.detectDisease(
                image,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Crop image analyzed successfully",
                response
        ));
    }

    /**
     * Get the authenticated farmer's detection history.
     *
     * @param page page number (0-indexed, default 0)
     * @param size results per page (default 10)
     * @param userDetails injected from JWT context
     * @return paginated detection history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<DiseaseDetectionResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<DiseaseDetectionResponse> history = diseaseDetectionService.getUserHistory(
                userDetails.getUsername(),
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Detection history retrieved",
                history
        ));
    }
}
