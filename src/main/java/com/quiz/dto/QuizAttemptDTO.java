package com.quiz.dto;

import com.quiz.model.QuizAttempt;
import com.quiz.model.QuizStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuizAttemptDTO {
    private Long id;
    private QuizStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalScore;
    private Integer obtainedScore;
    private List<QuizResponseDTO> responses;

    public QuizAttemptDTO(QuizAttempt attempt) {
        this.id = attempt.getId();
        this.status = attempt.getStatus();
        this.startTime = attempt.getStartTime().toLocalDateTime();
        this.endTime = attempt.getEndTime() != null ? attempt.getEndTime().toLocalDateTime() : null;
        this.totalScore = attempt.getTotalScore();
        this.obtainedScore = attempt.getObtainedScore();
        if (attempt.getResponses() != null) {
            this.responses = attempt.getResponses().stream()
                .map(QuizResponseDTO::new)
                .collect(Collectors.toList());
        }
    }
} 