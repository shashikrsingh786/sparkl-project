package com.quiz.repository;

import com.quiz.model.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {
} 