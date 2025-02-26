package com.quiz.repository;

import com.quiz.model.QuizAttempt;
import com.quiz.model.User;
import com.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizOrderByIdDesc(Quiz quiz);
    List<QuizAttempt> findByUserOrderByIdDesc(User user);
    Optional<QuizAttempt> findByQuizAndUser(Quiz quiz, User user);
} 