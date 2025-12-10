package com.questiongenerator.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questiongenerator.dto.QuestionRequest;
import com.questiongenerator.dto.QuestionResponse;
import com.questiongenerator.entity.QuestionHistory;
import com.questiongenerator.repository.QuestionHistoryRepository;

@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.provider:huggingface}")
    private String aiProvider;

    @Autowired
    private HuggingFaceAIProvider huggingFaceAIProvider;

    @Autowired
    private OpenAIAIProvider openAIAIProvider;

    @Autowired
    private QuestionHistoryRepository historyRepository;

    private static final double TEMPERATURE_EASY = 0.6;
    private static final double TEMPERATURE_MEDIUM = 0.7;
    private static final double TEMPERATURE_HARD = 0.8;

    private static String sanitizeJsonStringLiterals(String raw) {
        if (raw == null || raw.isEmpty())
            return raw;

        StringBuilder out = new StringBuilder(raw.length() + 64);
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < raw.length(); ++i) {
            char c = raw.charAt(i);

            if (escape) {
                // previous char was backslash - validate escape sequence
                // Valid JSON escape sequences: " \ / b f n r t u
                if (c == '"' || c == '\\' || c == '/' || c == 'b' || c == 'f' ||
                        c == 'n' || c == 'r' || c == 't' || c == 'u') {
                    // Valid escape sequence - keep it
                    out.append(c);
                } else {
                    // Invalid escape sequence - escape the backslash and the character
                    // This converts \S to \\S (escaped backslash + S)
                    out.append('\\').append(c);
                }
                escape = false;
                continue;
            }

            if (c == '\\') {
                // enter escape mode
                escape = true;
                out.append(c);
                continue;
            }

            if (c == '"') {
                // toggle inString state and copy the quote
                inString = !inString;
                out.append(c);
                continue;
            }

            if (inString) {
                // We are inside a JSON string and not after a backslash.
                // Escape control chars that are illegal unescaped in JSON strings.
                if (c == '\n') {
                    out.append("\\n");
                    continue;
                } else if (c == '\r') {
                    out.append("\\r");
                    continue;
                } else if (c == '\t') {
                    out.append("\\t");
                    continue;
                } else if (c == '\b') {
                    out.append("\\b");
                    continue;
                } else if (c == '\f') {
                    out.append("\\f");
                    continue;
                } else if (c >= 0 && c < 0x20) {
                    // Other C0 controls -> use unicode escape
                    out.append(String.format("\\u%04x", (int) c));
                    continue;
                }
            }

            // default: copy character unchanged
            out.append(c);
        }

        // If string ended while an escape was active, we need to escape the backslash
        if (escape) {
            // Remove the last backslash and add an escaped one
            out.setLength(out.length() - 1);
            out.append("\\\\");
        }

        return out.toString();
    }

    /**
     * Removes comments from JSON string (both // and block comment style)
     * This handles cases where AI includes comments in generated JSON
     */
    private static String removeJsonComments(String json) {
        if (json == null || json.isEmpty())
            return json;

        StringBuilder result = new StringBuilder(json.length());
        boolean inString = false;
        boolean escape = false;
        int i = 0;

        while (i < json.length()) {
            char c = json.charAt(i);

            if (escape) {
                result.append(c);
                escape = false;
                i++;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                escape = true;
                i++;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                result.append(c);
                i++;
                continue;
            }

            if (!inString) {
                // Check for // comments
                if (c == '/' && i + 1 < json.length() && json.charAt(i + 1) == '/') {
                    // Skip to end of line
                    while (i < json.length() && json.charAt(i) != '\n' && json.charAt(i) != '\r') {
                        i++;
                    }
                    continue;
                }

                // Check for /* */ comments
                if (c == '/' && i + 1 < json.length() && json.charAt(i + 1) == '*') {
                    // Skip to end of comment
                    i += 2;
                    while (i + 1 < json.length()) {
                        if (json.charAt(i) == '*' && json.charAt(i + 1) == '/') {
                            i += 2;
                            break;
                        }
                        i++;
                    }
                    continue;
                }
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    public QuestionResponse generateQuestions(QuestionRequest request) {
        try {
            String prompt = buildPrompt(request);
            double temperature = getTemperature(request.getDifficulty());

            // Select AI provider based on configuration
            AIProvider provider = getAIProvider();
            logger.info("Using AI Provider: {}", aiProvider);

            String response = provider.generateResponse(prompt, temperature);

            if (response == null || response.trim().isEmpty()) {
                logger.error("AI provider returned empty response");
                throw new RuntimeException("AI provider returned empty response");
            }

            QuestionResponse result = parseResponse(response, request.isIncludeAnswers());

            // Save to history database
            saveToHistory(request, result);

            return result;
        } catch (Exception e) {
            logger.error("Error generating questions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate questions: " + e.getMessage(), e);
        }
    }

    private void saveToHistory(QuestionRequest request, QuestionResponse response) {
        try {
            String technicalJson = objectMapper.writeValueAsString(response.getTechnicalQuestions());
            String behavioralJson = objectMapper.writeValueAsString(response.getBehavioralQuestions());

            QuestionHistory history = new QuestionHistory(
                    request.getRole(),
                    request.getTopic(),
                    request.getDifficulty(),
                    technicalJson,
                    behavioralJson,
                    request.isIncludeAnswers());

            historyRepository.save(history);
            logger.info("Saved question history for role: {}, topic: {}", request.getRole(), request.getTopic());
        } catch (Exception e) {
            // Log error but don't fail the main request
            logger.error("Failed to save question history: {}", e.getMessage(), e);
        }
    }

    private AIProvider getAIProvider() {
        return switch (aiProvider.toLowerCase()) {
            case "openai" -> {
                logger.info("Attempting to use OpenAI provider");
                yield openAIAIProvider;
            }
            case "huggingface", "hf" -> {
                logger.info("Attempting to use Hugging Face provider");
                yield huggingFaceAIProvider;
            }
            default -> {
                logger.warn("Unknown AI provider '{}', falling back to Hugging Face provider", aiProvider);
                yield huggingFaceAIProvider;
            }
        };
    }

    private String buildPrompt(QuestionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate interview questions for the following specifications:\n\n");
        prompt.append("Job Role: ").append(request.getRole()).append("\n");
        prompt.append("Topic: ").append(request.getTopic()).append("\n");
        prompt.append("Difficulty Level: ").append(request.getDifficulty()).append("\n\n");

        prompt.append("Please generate:\n");
        prompt.append("- 5 technical questions related to ").append(request.getTopic()).append("\n");
        prompt.append("- 3 behavioral questions relevant to ").append(request.getRole()).append("\n\n");

        if (request.isIncludeAnswers()) {
            prompt.append("Include sample answers for each question.\n\n");
        }

        prompt.append("Format your response as a JSON object with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"technicalQuestions\": [\n");
        prompt.append("    {\"question\": \"...\", \"answer\": \"...\"},\n");
        prompt.append("    ...\n");
        prompt.append("  ],\n");
        prompt.append("  \"behavioralQuestions\": [\n");
        prompt.append("    {\"question\": \"...\", \"answer\": \"...\"}\n");
        prompt.append("    ...\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("Ensure questions are relevant, varied, and appropriate for the difficulty level.");
        if (!request.isIncludeAnswers()) {
            prompt.append(" Set \"answer\" to null for each question.");
        }

        return prompt.toString();
    }

    private double getTemperature(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> TEMPERATURE_EASY;
            case "medium" -> TEMPERATURE_MEDIUM;
            case "hard" -> TEMPERATURE_HARD;
            default -> TEMPERATURE_MEDIUM;
        };
    }

    private QuestionResponse parseResponse(String response, @SuppressWarnings("unused") boolean includeAnswers) {
        List<QuestionResponse.Question> technicalQuestions = new ArrayList<>();
        List<QuestionResponse.Question> behavioralQuestions = new ArrayList<>();

        try {
            logger.debug("Raw AI response:\n{}", response);

            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("Empty AI response");
            }

            // 1) Remove common fenced code block markers if present
            String cleaned = response.trim();

            // Remove triple-backtick fences possibly with language tag (e.g. ```json)
            cleaned = cleaned.replaceAll("(?s)```\\s*json\\b", "```"); // normalize ```json -> ```
            // Now remove any leading/trailing triple backticks
            if (cleaned.startsWith("```")) {
                // drop leading fence
                cleaned = cleaned.substring(3).trim();
            }
            if (cleaned.endsWith("```")) {
                // drop trailing fence
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }

            // Also remove single backticks that sometimes wrap inline code blocks
            if (cleaned.startsWith("`") && cleaned.endsWith("`") && cleaned.length() > 1) {
                cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
            }

            // 2) Extract the JSON by finding the first '{' or '[' and the last '}' or ']'
            int firstCurly = cleaned.indexOf('{');
            int firstSquare = cleaned.indexOf('[');
            int firstOpen = -1;
            if (firstCurly == -1 && firstSquare == -1) {
                // No obvious JSON start — try to find a substring after a "json" marker
                int jsonMarker = cleaned.toLowerCase().indexOf("{");
                if (jsonMarker >= 0)
                    firstOpen = jsonMarker;
            } else if (firstCurly == -1) {
                firstOpen = firstSquare;
            } else if (firstSquare == -1) {
                firstOpen = firstCurly;
            } else {
                firstOpen = Math.min(firstCurly, firstSquare);
            }

            if (firstOpen == -1) {
                // Give up extraction, try using the whole cleaned string
                logger.warn(
                        "Could not find explicit JSON start ('{' or '['). Attempting to parse entire cleaned response.");
                firstOpen = 0;
            }

            // For the closing bracket, pick the last '}' or ']' whichever is later
            int lastCurly = cleaned.lastIndexOf('}');
            int lastSquare = cleaned.lastIndexOf(']');
            int lastClose = Math.max(lastCurly, lastSquare);
            if (lastClose < firstOpen) {
                // Something odd — fallback to end of string
                lastClose = cleaned.length() - 1;
            }

            String jsonContent = cleaned.substring(firstOpen, lastClose + 1).trim();

            logger.debug("Extracted JSON content (first 500 chars):\n{}",
                    jsonContent.length() > 500 ? jsonContent.substring(0, 500) + "..." : jsonContent);

            // Remove comments from JSON (both // and /* */ style)
            String jsonWithoutComments = removeJsonComments(jsonContent);

            // Parse JSON using Jackson with comments enabled (as fallback)
            JsonFactory factory = JsonFactory.builder()
                    .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                    .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                    .build();
            ObjectMapper mapper = new ObjectMapper(factory);
            String sanitized = sanitizeJsonStringLiterals(jsonWithoutComments);

            // Try to parse, but handle incomplete JSON gracefully
            JsonNode rootNode;
            try {
                rootNode = mapper.readTree(sanitized);
            } catch (Exception parseException) {
                // Check if it's an incomplete JSON error
                String errorMsg = parseException.getMessage();
                if (errorMsg != null && (errorMsg.contains("end-of-input") ||
                        errorMsg.contains("Unexpected EOF") ||
                        errorMsg.contains("expected close marker"))) {
                    // JSON is incomplete - try to extract what we can
                    logger.warn("JSON response appears incomplete. Attempting to extract partial data.");
                    rootNode = tryParseIncompleteJson(sanitized, mapper);
                } else {
                    // Re-throw if it's a different error
                    throw parseException;
                }
            }

            // Parse technical questions
            if (rootNode != null && rootNode.has("technicalQuestions")
                    && rootNode.get("technicalQuestions").isArray()) {
                for (JsonNode node : rootNode.get("technicalQuestions")) {
                    String question = node.has("question") ? node.get("question").asText() : "";
                    String answer = node.has("answer") && !node.get("answer").isNull()
                            ? node.get("answer").asText()
                            : null;
                    if (!question.isEmpty()) {
                        technicalQuestions.add(new QuestionResponse.Question(question, answer));
                    }
                }
            } else {
                logger.warn("No 'technicalQuestions' array found in AI response JSON.");
            }

            // Parse behavioral questions
            if (rootNode != null && rootNode.has("behavioralQuestions")
                    && rootNode.get("behavioralQuestions").isArray()) {
                for (JsonNode node : rootNode.get("behavioralQuestions")) {
                    String question = node.has("question") ? node.get("question").asText() : "";
                    String answer = node.has("answer") && !node.get("answer").isNull()
                            ? node.get("answer").asText()
                            : null;
                    if (!question.isEmpty()) {
                        behavioralQuestions.add(new QuestionResponse.Question(question, answer));
                    }
                }
            } else {
                logger.warn("No 'behavioralQuestions' array found in AI response JSON.");
            }

            // If we got some questions but not enough, log a warning
            if (technicalQuestions.size() < 5 || behavioralQuestions.size() < 3) {
                logger.warn("Received incomplete response: {} technical, {} behavioral questions (expected 5 and 3)",
                        technicalQuestions.size(), behavioralQuestions.size());
            }

        } catch (Exception e) {
            // Log the full response + exception for easier debugging
            logger.error("Error parsing AI response: {}. Raw response (first 1000 chars): {}",
                    e.getMessage(),
                    response != null && response.length() > 1000 ? response.substring(0, 1000) + "..." : response,
                    e);

            // Fallback: create sample questions if parsing fails
            if (technicalQuestions.isEmpty()) {
                technicalQuestions.add(new QuestionResponse.Question(
                        "Error parsing response. The AI response may have been incomplete. Please try again.", null));
            }
            if (behavioralQuestions.isEmpty()) {
                behavioralQuestions.add(new QuestionResponse.Question(
                        "Error parsing response. The AI response may have been incomplete. Please try again.", null));
            }
        }

        return new QuestionResponse(technicalQuestions, behavioralQuestions);
    }

    /**
     * Attempts to parse incomplete JSON by trying to close open brackets/braces
     */
    private JsonNode tryParseIncompleteJson(String jsonContent, ObjectMapper mapper) {
        try {
            // Try to close incomplete JSON by adding missing closing brackets
            StringBuilder fixed = new StringBuilder(jsonContent);

            // Count open/close brackets
            long openBraces = jsonContent.chars().filter(ch -> ch == '{').count();
            long closeBraces = jsonContent.chars().filter(ch -> ch == '}').count();
            long openBrackets = jsonContent.chars().filter(ch -> ch == '[').count();
            long closeBrackets = jsonContent.chars().filter(ch -> ch == ']').count();

            // Add missing closing brackets
            for (long i = closeBrackets; i < openBrackets; i++) {
                fixed.append(']');
            }
            for (long i = closeBraces; i < openBraces; i++) {
                fixed.append('}');
            }

            return mapper.readTree(fixed.toString());
        } catch (Exception e) {
            logger.warn("Failed to fix incomplete JSON: {}", e.getMessage());
            // Return empty JSON object as fallback
            try {
                return mapper.readTree("{}");
            } catch (Exception ex) {
                return null;
            }
        }
    }

}
