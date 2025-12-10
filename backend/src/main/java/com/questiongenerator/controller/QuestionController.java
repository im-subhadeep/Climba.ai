package com.questiongenerator.controller;

import com.questiongenerator.dto.QuestionRequest;
import com.questiongenerator.dto.QuestionResponse;
import com.questiongenerator.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:3000")
public class QuestionController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    
    @Autowired
    private QuestionService questionService;
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuestions(@Valid @RequestBody QuestionRequest request) {
        try {
            QuestionResponse response = questionService.generateQuestions(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating questions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating questions: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }
}

