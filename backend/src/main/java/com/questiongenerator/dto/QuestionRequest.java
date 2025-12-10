package com.questiongenerator.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionRequest {
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Topic is required")
    private String topic;
    
    @NotBlank(message = "Difficulty is required")
    private String difficulty;
    
    private boolean includeAnswers = false;
    
    public QuestionRequest() {}
    
    public QuestionRequest(String role, String topic, String difficulty, boolean includeAnswers) {
        this.role = role;
        this.topic = topic;
        this.difficulty = difficulty;
        this.includeAnswers = includeAnswers;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public boolean isIncludeAnswers() {
        return includeAnswers;
    }
    
    public void setIncludeAnswers(boolean includeAnswers) {
        this.includeAnswers = includeAnswers;
    }
}

