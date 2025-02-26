package com.quiz.dto;

import com.quiz.model.QuizResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuizResponseDTO {
    private Long id;
    private Long questionId;
    private String question;
    private String userOption;
    private boolean correct;
    private Integer marks;

    public QuizResponseDTO(QuizResponse response) {
        this.id = response.getId();
        this.questionId = response.getQuestion().getId();
        this.question = response.getQuestion().getQuestion();
        this.userOption = response.getUserOption();
        this.correct = response.isCorrect();
        this.marks = response.getMarks();
    }

    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getUserOption() {
        return userOption;
    }

    public void setUserOption(String userOption) {
        this.userOption = userOption;
    }
}