package com.questiongenerator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.questiongenerator.dto.HistoryResponse;
import com.questiongenerator.dto.QuestionResponse;
import com.questiongenerator.entity.QuestionHistory;
import com.questiongenerator.repository.QuestionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "http://localhost:3000")
public class HistoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private QuestionHistoryRepository historyRepository;
    
    @GetMapping
    public ResponseEntity<List<HistoryResponse>> getRecentHistory() {
        try {
            List<QuestionHistory> historyList = historyRepository.findTop20ByOrderByCreatedAtDesc();
            List<HistoryResponse> responseList = historyList.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            logger.error("Error fetching history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HistoryResponse> getHistoryById(@PathVariable Long id) {
        try {
            Optional<QuestionHistory> historyOpt = historyRepository.findById(id);
            if (historyOpt.isPresent()) {
                return ResponseEntity.ok(convertToResponse(historyOpt.get()));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching history by id: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        try {
            if (historyRepository.existsById(id)) {
                historyRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<HistoryResponse>> searchHistory(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty) {
        try {
            List<QuestionHistory> results;
            
            if (role != null && !role.isEmpty()) {
                results = historyRepository.findByRoleContainingIgnoreCaseOrderByCreatedAtDesc(role);
            } else if (topic != null && !topic.isEmpty()) {
                results = historyRepository.findByTopicContainingIgnoreCaseOrderByCreatedAtDesc(topic);
            } else if (difficulty != null && !difficulty.isEmpty()) {
                results = historyRepository.findByDifficultyIgnoreCaseOrderByCreatedAtDesc(difficulty);
            } else {
                results = historyRepository.findTop20ByOrderByCreatedAtDesc();
            }
            
            List<HistoryResponse> responseList = results.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            logger.error("Error searching history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private HistoryResponse convertToResponse(QuestionHistory history) {
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setRole(history.getRole());
        response.setTopic(history.getTopic());
        response.setDifficulty(history.getDifficulty());
        response.setIncludeAnswers(history.isIncludeAnswers());
        response.setCreatedAt(history.getCreatedAt());
        
        // Parse JSON strings to Question lists
        try {
            if (history.getTechnicalQuestions() != null) {
                List<QuestionResponse.Question> techQuestions = objectMapper.readValue(
                        history.getTechnicalQuestions(),
                        new TypeReference<List<QuestionResponse.Question>>() {}
                );
                response.setTechnicalQuestions(techQuestions);
            } else {
                response.setTechnicalQuestions(new ArrayList<>());
            }
            
            if (history.getBehavioralQuestions() != null) {
                List<QuestionResponse.Question> behavQuestions = objectMapper.readValue(
                        history.getBehavioralQuestions(),
                        new TypeReference<List<QuestionResponse.Question>>() {}
                );
                response.setBehavioralQuestions(behavQuestions);
            } else {
                response.setBehavioralQuestions(new ArrayList<>());
            }
        } catch (Exception e) {
            logger.error("Error parsing questions from history: {}", e.getMessage());
            response.setTechnicalQuestions(new ArrayList<>());
            response.setBehavioralQuestions(new ArrayList<>());
        }
        
        return response;
    }
}
