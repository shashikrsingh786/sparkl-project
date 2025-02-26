package com.quiz.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizSubmissionRequest {
    private List<QuizResponseDTO> responses;
    private LocalDateTime endTime;

    // Getters and Setters
    public List<QuizResponseDTO> getResponses() {
        return responses;
    }

    public void setResponses(List<QuizResponseDTO> responses) {
        this.responses = responses;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}