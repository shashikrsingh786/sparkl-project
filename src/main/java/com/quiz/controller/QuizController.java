package com.quiz.controller;

import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.Quiz;
import com.quiz.model.QuizQuestion;
import com.quiz.model.QuizAttempt;
import com.quiz.model.User;
import com.quiz.repository.QuizRepository;
import com.quiz.repository.QuizQuestionRepository;
import com.quiz.repository.QuizAttemptRepository;
import com.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.quiz.dto.QuizDTO;
import com.quiz.dto.QuizQuestionDTO;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuizQuestionRepository quizQuestionRepository;
    
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;
    
    @Autowired 
    private UserRepository userRepository;

    @GetMapping
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findByOrderByIdDesc()
            .stream()
            .map(QuizDTO::new)
            .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz, Authentication authentication) {
        // Get the username from authentication
        String username = authentication.getName();
        
        // Fetch the actual User entity from database
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        // Set the quiz creator
        quiz.setCreatedBy(currentUser);
        quiz.setActive(true);
        quiz.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        quiz.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        Quiz savedQuiz = quizRepository.save(quiz);
        return ResponseEntity.ok(savedQuiz);
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuizQuestionDTO>> getQuizQuestions(@PathVariable Long quizId) {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdWithQuestion(quizId);
        List<QuizQuestionDTO> questionDTOs = questions.stream()
                .map(QuizQuestionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questionDTOs);
    }
    
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<?> addQuestionsToQuiz(@PathVariable Long quizId, 
                                               @RequestBody List<QuizQuestion> quizQuestions) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
                
        List<QuizQuestion> savedQuestions = new ArrayList<>();
        System.out.println("quizQuestions"+quizQuestions);
        for (QuizQuestion quizQuestion : quizQuestions) {
            System.out.println("quizQuestionwwwwwwwwwwww"+quizQuestion);
            // Validate that question is not null
            if (quizQuestion.getQuestion() == null || quizQuestion.getQuestion().getId() == null) {
                return ResponseEntity.badRequest()
                    .body("Question ID cannot be null for quiz question");
            }
            
            // Validate marks
            if (quizQuestion.getMarks() == null || quizQuestion.getMarks() <= 0) {
                return ResponseEntity.badRequest()
                    .body("Marks must be a positive number");
            }
            
            // Validate question order
            if (quizQuestion.getQuestionOrder() == null) {
                return ResponseEntity.badRequest()
                    .body("Question order cannot be null");
            }
            
            quizQuestion.setQuiz(quiz);
            quizQuestion.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            savedQuestions.add(quizQuestionRepository.save(quizQuestion));
        }
        
        List<QuizQuestionDTO> questionDTOs = savedQuestions.stream()
                .map(QuizQuestionDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(questionDTOs);
    }
    
    @GetMapping("/{quizId}/participants")
    public List<QuizAttempt> getQuizParticipants(@PathVariable Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        return quizAttemptRepository.findByQuizOrderByIdDesc(quiz);
    }
    
    @GetMapping("/{quizId}/response/{userId}")
    public ResponseEntity<QuizAttempt> getQuizResponse(@PathVariable Long quizId, 
                                                     @PathVariable Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        QuizAttempt attempt = quizAttemptRepository.findByQuizAndUser(quiz, user)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "quizId", quizId));
                
        return ResponseEntity.ok(attempt);
    }
}