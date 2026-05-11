package com.farmerassistant.service.impl;

import com.farmerassistant.dto.response.DiseaseDetectionResponse;
import com.farmerassistant.entity.DiseaseLog;
import com.farmerassistant.entity.User;
import com.farmerassistant.exception.InvalidImageException;
import com.farmerassistant.exception.ResourceNotFoundException;
import com.farmerassistant.repository.DiseaseLogRepository;
import com.farmerassistant.repository.UserRepository;
import com.farmerassistant.service.DiseaseDetectionService;
import com.farmerassistant.service.OpenAiService;
import com.farmerassistant.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of DiseaseDetectionService.
 *
 * Flow:
 * 1. Validate image (type + size)
 * 2. Check rate limit
 * 3. Call OpenAI Vision API
 * 4. Parse structured AI response
 * 5. Persist result to DB
 * 6. Return DTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiseaseDetectionServiceImpl implements DiseaseDetectionService {

    private final OpenAiService openAiService;
    private final RateLimiterService rateLimiterService;
    private final DiseaseLogRepository diseaseLogRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.allowed-types}")
    private String allowedTypesConfig;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    @Transactional
    public DiseaseDetectionResponse detectDisease(MultipartFile image, String userEmail) {
        log.info("Disease detection requested by: {}, file: {}", userEmail, image.getOriginalFilename());

        // 1. Validate the uploaded image
        validateImage(image);

        // 2. Apply per-user rate limiting
        rateLimiterService.checkDiseaseRateLimit(userEmail);

        // 3. Load the user entity
        User user = userRepository.findByEmailAndIsActiveTrue(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // 4. Read image bytes and call OpenAI Vision API
        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException("Failed to read image file: " + e.getMessage());
        }

        //String aiResponse = openAiService.analyzeCropImage(imageBytes, image.getContentType());
        Map<String, Object> aiResponse = openAiService.analyzeCropImage(imageBytes, image.getContentType());
        log.debug("Raw AI response: {}", aiResponse);

        // 5. Parse the structured AI response
//        ParsedDiseaseResult parsed = parseAiResponse(aiResponse);
        String disease = (String) aiResponse.get("disease");
        Double confidence = (Double) aiResponse.get("confidence");

        List<String> treatmentList = (List<String>) aiResponse.get("treatment");
        List<String> precautionList = (List<String>) aiResponse.get("precautions");

        String cure = (treatmentList != null && !treatmentList.isEmpty())
                ? treatmentList.get(0)
                : "No treatment available";

        String prevention = (precautionList != null && !precautionList.isEmpty())
                ? precautionList.get(0)
                : "No precautions available";

        // 6. Persist result to disease_logs table
        DiseaseLog diseaseLog = DiseaseLog.builder()
                .user(user)
                .imageName(image.getOriginalFilename())
                .imageContentType(image.getContentType())
                .diseaseName(disease)
                .confidenceLevel(String.valueOf(confidence))
                .cure(cure)
                .preventionTips(prevention)
                .rawAiResponse(aiResponse.toString())
                .status("SUCCESS")
                .build();

        diseaseLog = diseaseLogRepository.save(diseaseLog);
        log.info("Disease detection saved: id={}, disease={}", diseaseLog.getId(), disease);

        // 7. Build and return response DTO
        return DiseaseDetectionResponse.builder()
                .logId(diseaseLog.getId())
                .imageName(image.getOriginalFilename())
                .diseaseName(disease)
                .confidenceLevel(String.valueOf(confidence))
                .cure(cure)
                .preventionTips(prevention)
                .status("SUCCESS")
                .detectedAt(diseaseLog.getDetectedAt())
                .message("Crop image analyzed successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseDetectionResponse> getUserHistory(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmailAndIsActiveTrue(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return diseaseLogRepository
                .findByUserIdOrderByDetectedAtDesc(user.getId(), pageable)
                .map(log -> DiseaseDetectionResponse.builder()
                        .logId(log.getId())
                        .imageName(log.getImageName())
                        .diseaseName(log.getDiseaseName())
                        .confidenceLevel(log.getConfidenceLevel())
                        .cure(log.getCure())
                        .preventionTips(log.getPreventionTips())
                        .status(log.getStatus())
                        .detectedAt(log.getDetectedAt())
                        .build()
                );
    }

    // ─── Validation ────────────────────────────────────────────────────────────

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Image file is required and cannot be empty.");
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageException("Image file exceeds the 10MB size limit.");
        }

        List<String> allowedTypes = Arrays.asList(allowedTypesConfig.split(","));
        String contentType = image.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.trim())) {
            throw new InvalidImageException(
                "Invalid image type: '" + contentType + "'. Allowed types: " + allowedTypesConfig
            );
        }
    }

    // ─── AI Response Parser ────────────────────────────────────────────────────

    /**
     * Parses the structured response from OpenAI into individual fields.
     *
     * Expected format:
     * DISEASE_NAME: ...
     * CONFIDENCE: ...
     * CURE: ...
     * PREVENTION: ...
     */
//    private ParsedDiseaseResult parseAiResponse(String aiResponse) {
//        String diseaseName = extractField(aiResponse, "DISEASE_NAME");
//        String confidence = extractField(aiResponse, "CONFIDENCE");
//        String cure = extractField(aiResponse, "CURE");
//        String prevention = extractField(aiResponse, "PREVENTION");
//
//        return new ParsedDiseaseResult(
//            diseaseName.isEmpty() ? "Unknown" : diseaseName,
//            confidence.isEmpty() ? "Unknown" : confidence,
//            cure.isEmpty() ? aiResponse : cure,
//            prevention.isEmpty() ? "Please consult a local agronomist." : prevention
//        );
//    }

    /** Extracts a labeled field value from the AI response text. */
//    private String extractField(String text, String fieldName) {
//        // Match "FIELD_NAME: value" capturing until the next field or end of string
//        Pattern pattern = Pattern.compile(
//            fieldName + ":\\s*(.+?)(?=\\n[A-Z_]+:|$)",
//            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
//        );
//        Matcher matcher = pattern.matcher(text);
//        if (matcher.find()) {
//            return matcher.group(1).trim();
//        }
//        return "";
//    }

    /** Internal record to hold parsed AI response fields. */
//    private record ParsedDiseaseResult(
//        String diseaseName,
//        String confidence,
//        String cure,
//        String prevention
//    ) {}
}
