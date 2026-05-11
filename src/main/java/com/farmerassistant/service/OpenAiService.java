package com.farmerassistant.service;

import com.farmerassistant.exception.OpenAiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for all OpenAI API interactions.
 *
 * Methods:
 *  - analyzeCropImage()  → GPT-4o Vision for disease detection
 *  - askFarmerQuery()    → GPT-4o Chat for farming Q&A
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final WebClient openAiWebClient;

    @Value("${openai.model.chat}")
    private String chatModel;

    @Value("${openai.model.vision}")
    private String visionModel;

    @Value("${openai.max-tokens}")
    private int maxTokens;

    // ─── System prompts ───────────────────────────────────────────────────────

    private static final String DISEASE_SYSTEM_PROMPT = """
        You are an expert agricultural plant pathologist AI assistant.
        Analyze the provided crop image and respond ONLY in the following structured format:

        DISEASE_NAME: [name of the disease, or "Healthy" if no disease found]
        CONFIDENCE: [Low / Medium / High]
        CURE: [step-by-step treatment in simple language, max 150 words]
        PREVENTION: [3-5 bullet prevention tips in simple language]

        Be concise, practical, and use language a farmer can easily understand.
        If the image is not a crop/plant image, respond with:
        DISEASE_NAME: Invalid Image
        CONFIDENCE: N/A
        CURE: Please upload a clear photo of a crop or plant.
        PREVENTION: N/A
        """;

    private static final String CHAT_SYSTEM_PROMPT = """
        You are a friendly and knowledgeable farming assistant AI.
        Your role is to help farmers with practical farming advice.

        Guidelines:
        - Use simple, clear language that any farmer can understand
        - Be concise and actionable (max 200 words per response)
        - Focus on practical steps, not theory
        - If asked about non-farming topics, politely redirect to farming
        - Always consider local farming conditions
        - Provide season-specific advice when relevant
        """;

    // ─── Public API Methods ───────────────────────────────────────────────────

    /**
     * Analyzes a crop image using OpenAI GPT-4o Vision API to detect diseases.
     *
     * @param imageBytes raw bytes of the uploaded image
     * @param contentType MIME type of the image (e.g., image/jpeg)
     * @return raw string response from OpenAI in structured format
     * @throws OpenAiException if the API call fails
     */
    public Map<String, Object> analyzeCropImage(byte[] imageBytes, String contentType) {
        log.info("Sending crop image to OpenAI Vision API ({} bytes, type: {})",
                imageBytes.length, contentType);

        // Encode image to base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String imageUrl = "data:" + contentType + ";base64," + base64Image;

        // Build request payload with image_url content block
        Map<String, Object> requestBody = Map.of(
            "model", visionModel,
            "max_tokens", maxTokens,
            "messages", List.of(
                Map.of("role", "system", "content", DISEASE_SYSTEM_PROMPT),
                Map.of("role", "user", "content", List.of(
                    Map.of(
                        "type", "image_url",
                        "image_url", Map.of("url", imageUrl, "detail", "high")
                    ),
                    Map.of(
                        "type", "text",
                        "text", "Please analyze this crop image for diseases."
                    )
                ))
            )
        );

//        return callOpenAiApi(requestBody, "vision");
        String rawResponse = callOpenAiApi(requestBody, "vision");
        return parseAiResponse(rawResponse);
    }

    /**
     * Sends a farmer's question to OpenAI GPT-4o Chat API and returns the answer.
     *
     * @param question the farmer's question in plain text
     * @return AI-generated answer string
     * @throws OpenAiException if the API call fails
     */
    public String askFarmerQuery(String question) {
        log.info("Sending farming query to OpenAI Chat API: [{}...]",
                question.length() > 50 ? question.substring(0, 50) : question);

        Map<String, Object> requestBody = Map.of(
            "model", chatModel,
            "max_tokens", maxTokens,
            "messages", List.of(
                Map.of("role", "system", "content", CHAT_SYSTEM_PROMPT),
                Map.of("role", "user", "content", question)
            )
        );

        return callOpenAiApi(requestBody, "chat");
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    /**
     * Executes the OpenAI API call and extracts the text content from the response.
     */
    @SuppressWarnings("unchecked")
    private String callOpenAiApi(Map<String, Object> requestBody, String type) {
        try {
            Map<String, Object> response = openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new OpenAiException("Received null response from OpenAI API");
            }

            // Navigate: response → choices[0] → message → content
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new OpenAiException("No choices in OpenAI response");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            log.info("OpenAI {} API responded successfully", type);
            return content;

        } catch (WebClientResponseException e) {
            log.error("OpenAI API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new OpenAiException("OpenAI API error: " + e.getStatusCode() + " - " + e.getMessage(), e);
        } catch (OpenAiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling OpenAI API: {}", e.getMessage(), e);
            throw new OpenAiException("Failed to communicate with AI service: " + e.getMessage(), e);
        }
    }
    private Map<String, Object> parseAiResponse(String content) {
        String disease = "Unknown";
        double confidence = 0.5;
        String cure = "";
        String prevention = "";

        try {
            String[] lines = content.split("\n");

            for (String line : lines) {
                if (line.startsWith("DISEASE_NAME:")) {
                    disease = line.replace("DISEASE_NAME:", "").trim();
                } else if (line.startsWith("CONFIDENCE:")) {
                    String conf = line.replace("CONFIDENCE:", "").trim().toLowerCase();

                    switch (conf) {
                        case "high":
                            confidence = 0.9;
                            break;
                        case "medium":
                            confidence = 0.6;
                            break;
                        case "low":
                            confidence = 0.3;
                            break;
                        default:
                            confidence = 0.5;
                    }
                } else if (line.startsWith("CURE:")) {
                    cure = line.replace("CURE:", "").trim();
                } else if (line.startsWith("PREVENTION:")) {
                    prevention = line.replace("PREVENTION:", "").trim();
                }
            }

        } catch (Exception e) {
            log.error("Error parsing AI response", e);
        }

        return Map.of(
                "disease", disease,
                "confidence", confidence,
                "treatment", List.of(cure),
                "precautions", List.of(prevention),
                "description", "AI analyzed crop condition"
        );
    }
}


