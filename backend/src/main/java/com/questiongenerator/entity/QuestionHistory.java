package com.questiongenerator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_history")
public class QuestionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private String topic;
    
    @Column(nullable = false)
    private String difficulty;
    
    @Column(columnDefinition = "TEXT")
    private String technicalQuestions;
    
    @Column(columnDefinition = "TEXT")
    private String behavioralQuestions;
    
    private boolean includeAnswers;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public QuestionHistory() {
        this.createdAt = LocalDateTime.now();
    }
    
    public QuestionHistory(String role, String topic, String difficulty, 
                          String technicalQuestions, String behavioralQuestions, 
                          boolean includeAnswers) {
        this.role = role;
        this.topic = topic;
        this.difficulty = difficulty;
        this.technicalQuestions = technicalQuestions;
        this.behavioralQuestions = behavioralQuestions;
        this.includeAnswers = includeAnswers;
        this.createdAt = LocalDateTime.now();
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
    
    public String getTechnicalQuestions() {
        return technicalQuestions;
    }
    
    public void setTechnicalQuestions(String technicalQuestions) {
        this.technicalQuestions = technicalQuestions;
    }
    
    public String getBehavioralQuestions() {
        return behavioralQuestions;
    }
    
    public void setBehavioralQuestions(String behavioralQuestions) {
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
