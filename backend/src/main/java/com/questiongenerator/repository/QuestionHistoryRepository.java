package com.questiongenerator.repository;

import com.questiongenerator.entity.QuestionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionHistoryRepository extends JpaRepository<QuestionHistory, Long> {
    
    // Get recent history ordered by creation date
    List<QuestionHistory> findTop20ByOrderByCreatedAtDesc();
    
    // Search by role
    List<QuestionHistory> findByRoleContainingIgnoreCaseOrderByCreatedAtDesc(String role);
    
    // Search by topic
    List<QuestionHistory> findByTopicContainingIgnoreCaseOrderByCreatedAtDesc(String topic);
    
    // Search by difficulty
    List<QuestionHistory> findByDifficultyIgnoreCaseOrderByCreatedAtDesc(String difficulty);
}
