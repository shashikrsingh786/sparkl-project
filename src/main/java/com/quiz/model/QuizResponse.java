package com.quiz.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "quiz_responses")
public class QuizResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    @JsonBackReference(value = "attempt-responses")
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_option", nullable = false, columnDefinition = "TEXT")
    private String userOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    @Column(nullable = false)
    private Integer marks;

    @Column(name = "response_time", nullable = false)
    private Timestamp responseTime;

    @PrePersist
    protected void onCreate() {
        responseTime = new Timestamp(System.currentTimeMillis());
    }

    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public void setUserOption(String userOption) {
        this.userOption = userOption;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }
}