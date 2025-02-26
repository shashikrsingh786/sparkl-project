package com.quiz.dto;

import com.quiz.model.QuizQuestion;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizQuestionDTO {
    private Long id;
    private Long quizId;
    private Long questionId;
    private String question;
    private Integer marks;
    private Integer questionOrder;
    private LocalDateTime createdAt;
    
    public QuizQuestionDTO(QuizQuestion quizQuestion) {
        this.id = quizQuestion.getId();
        if (quizQuestion.getQuiz() != null) {
            this.quizId = quizQuestion.getQuiz().getId();
        }
        if (quizQuestion.getQuestion() != null) {
            this.questionId = quizQuestion.getQuestion().getId();
            this.question = quizQuestion.getQuestion().getQuestion();
        }
        this.marks = quizQuestion.getMarks();
        this.questionOrder = quizQuestion.getQuestionOrder();
        this.createdAt = quizQuestion.getCreatedAt().toLocalDateTime();
    }
}