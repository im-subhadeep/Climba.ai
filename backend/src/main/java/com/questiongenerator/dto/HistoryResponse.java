package com.questiongenerator.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HistoryResponse {
    private Long id;
    private String role;
    private String topic;
    private String difficulty;
    private List<QuestionResponse.Question> technicalQuestions;
    private List<QuestionResponse.Question> behavioralQuestions;
    private boolean includeAnswers;
    private LocalDateTime createdAt;
    
    public HistoryResponse() {}
    
    public HistoryResponse(Long id, String role, String topic, String difficulty,
                          List<QuestionResponse.Question> technicalQuestions,
                          List<QuestionResponse.Question> behavioralQuestions,
                          boolean includeAnswers, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.topic = topic;
        this.difficulty = difficulty;
        this.technicalQuestions = technicalQuestions;
        this.behavioralQuestions = behavioralQuestions;
        this.includeAnswers = includeAnswers;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public List<QuestionResponse.Question> getTechnicalQuestions() {
        return technicalQuestions;
    }
    
    public void setTechnicalQuestions(List<QuestionResponse.Question> technicalQuestions) {
        this.technicalQuestions = technicalQuestions;
    }
    
    public List<QuestionResponse.Question> getBehavioralQuestions() {
        return behavioralQuestions;
    }
    
    public void setBehavioralQuestions(List<QuestionResponse.Question> behavioralQuestions) {
        this.behavioralQuestions = behavioralQuestions;
    }
    
    public boolean isIncludeAnswers() {
        return includeAnswers;
    }
    
    public void setIncludeAnswers(boolean includeAnswers) {
        this.includeAnswers = includeAnswers;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
