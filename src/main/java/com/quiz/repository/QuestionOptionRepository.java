package com.quiz.repository;

import com.quiz.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionIdAndCorrect(Long questionId, boolean correct);
} 