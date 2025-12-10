package com.questiongenerator.dto;

import java.util.List;

public class QuestionResponse {
    private List<Question> technicalQuestions;
    private List<Question> behavioralQuestions;
    
    public QuestionResponse() {}
    
    public QuestionResponse(List<Question> technicalQuestions, List<Question> behavioralQuestions) {
        this.technicalQuestions = technicalQuestions;
        this.behavioralQuestions = behavioralQuestions;
    }
    
    public List<Question> getTechnicalQuestions() {
        return technicalQuestions;
    }
    
    public void setTechnicalQuestions(List<Question> technicalQuestions) {
        this.technicalQuestions = technicalQuestions;
    }
    
    public List<Question> getBehavioralQuestions() {
        return behavioralQuestions;
    }
    
    public void setBehavioralQuestions(List<Question> behavioralQuestions) {
        this.behavioralQuestions = behavioralQuestions;
    }
    
    public static class Question {
        private String question;
        private String answer;
        
        public Question() {}
        
        public Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public void setQuestion(String question) {
            this.question = question;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}

