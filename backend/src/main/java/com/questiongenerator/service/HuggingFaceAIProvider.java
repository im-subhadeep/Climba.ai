package com.questiongenerator.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HuggingFaceAIProvider implements AIProvider {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceAIProvider.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${huggingface.api.key:}")
    private String apiKey;

    // The model id (owner/model). Keep this value or change to any compatible model.
    private static final String MODEL = "Qwen/Qwen2.5-7B-Instruct";

    // Use the router OpenAI-compatible endpoint (chat completions).
    // This is the recommended way to call Hugging Face Router.
    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";

    @Override
    public String generateResponse(String prompt, double temperature) throws Exception {
        logger.info("Using Hugging Face API with model: {}", MODEL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.trim().isEmpty()) {
            headers.setBearerAuth(apiKey.trim());
            logger.info("Using Hugging Face API key");
        } else {
            logger.warn("No Hugging Face API key provided - some models may not work without authentication");
        }

        // Build OpenAI-compatible body for /v1/chat/completions
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);

        // messages array (chat API)
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(userMessage);
        requestBody.put("messages", messages);

        // generation parameters
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 7500); // Increased for complete JSON responses
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            logger.debug("Calling Hugging Face Router API: {}", API_URL);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

            logger.debug("Response status: {}", response.getStatusCode());
            logger.debug("Response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMsg = "Hugging Face API returned status: " + response.getStatusCode();
                if (response.getBody() != null) {
                    errorMsg += " - " + response.getBody();
                }
                throw new RuntimeException(errorMsg);
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Hugging Face API returned empty response");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(responseBody);

            // New router (OpenAI-compatible) — try to extract content from choices
            String generatedText = null;

            // Try chat completion shape: { choices: [ { message: { role, content } } ] }
            if (jsonResponse.has("choices") && jsonResponse.get("choices").isArray() && jsonResponse.get("choices").size() > 0) {
                JsonNode firstChoice = jsonResponse.get("choices").get(0);
                // chat-style: choices[0].message.content
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    generatedText = firstChoice.get("message").get("content").asText();
                }
                // completions-style: choices[0].text
                else if (firstChoice.has("text")) {
                    generatedText = firstChoice.get("text").asText();
                }
            }

            // Fallbacks to older inference API shapes
            if ((generatedText == null || generatedText.trim().isEmpty())) {
                if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                    JsonNode firstResult = jsonResponse.get(0);
                    if (firstResult.has("generated_text")) {
                        generatedText = firstResult.get("generated_text").asText();
                    } else if (firstResult.has("text")) {
                        generatedText = firstResult.get("text").asText();
                    }
                } else if (jsonResponse.has("generated_text")) {
                    generatedText = jsonResponse.get("generated_text").asText();
                } else if (jsonResponse.has("text")) {
                    generatedText = jsonResponse.get("text").asText();
                }
            }

            if (generatedText == null || generatedText.trim().isEmpty()) {
                logger.error("Could not extract generated text from response: {}", responseBody);
                throw new RuntimeException("Hugging Face API returned response but could not extract generated text. Response: " + responseBody);
            }

            return generatedText;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = "Hugging Face API error: " + e.getStatusCode() + " - " +
                    (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : "No error details");
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            logger.error("Error calling Hugging Face API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Hugging Face API: " + e.getMessage(), e);
        }
    }
}
