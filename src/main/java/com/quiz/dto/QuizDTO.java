package com.quiz.dto;

import lombok.Data;
import java.sql.Timestamp;
import com.quiz.model.Quiz;

@Data
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer duration;
    private String createdByUsername;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Add constructor to convert from Quiz entity
    public QuizDTO(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.description = quiz.getDescription();
        this.totalScore = quiz.getTotalScore();
        this.duration = quiz.getDuration();
        this.createdByUsername = quiz.getCreatedBy() != null ? quiz.getCreatedBy().getUsername() : null;
        this.active = quiz.isActive();
        this.createdAt = quiz.getCreatedAt();
        this.updatedAt = quiz.getUpdatedAt();
    }
}