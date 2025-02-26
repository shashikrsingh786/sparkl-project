package com.quiz.repository;

import com.quiz.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    @Query("SELECT qq FROM QuizQuestion qq JOIN FETCH qq.question WHERE qq.quiz.id = :quizId ORDER BY qq.questionOrder")
    List<QuizQuestion> findByQuizIdWithQuestion(@Param("quizId") Long quizId);
} 