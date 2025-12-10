package com.questiongenerator.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAIAIProvider implements AIProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIAIProvider.class);
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Override
    public String generateResponse(String prompt, double temperature) throws Exception {
        logger.info("Using OpenAI API");
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("your-api-key")) {
            throw new RuntimeException("OpenAI API key is not configured");
        }
        
        OpenAiService service = new OpenAiService(apiKey);
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
            "You are an expert interview question generator. Generate questions in the exact JSON format specified."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
        
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .temperature(temperature)
                .maxTokens(2000)
                .build();
        
        var completionResponse = service.createChatCompletion(completionRequest);
        
        if (completionResponse == null || completionResponse.getChoices() == null 
                || completionResponse.getChoices().isEmpty()) {
            throw new RuntimeException("OpenAI API returned empty response");
        }
        
        String response = completionResponse.getChoices().get(0).getMessage().getContent();
        
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("OpenAI API returned empty content");
        }
        
        return response;
    }
}

