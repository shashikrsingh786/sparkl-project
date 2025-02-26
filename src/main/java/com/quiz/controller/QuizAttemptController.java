package com.quiz.controller;

import com.quiz.model.QuizAttempt;
import com.quiz.model.QuestionOption;
import com.quiz.model.User;
import com.quiz.model.Quiz;
import com.quiz.repository.QuizAttemptRepository;
import com.quiz.repository.UserRepository;
import com.quiz.repository.QuizQuestionRepository;
import com.quiz.repository.QuizResponseRepository;
import com.quiz.repository.QuestionOptionRepository;
import com.quiz.repository.QuizRepository;
import com.quiz.dto.QuizSubmissionRequest;
import com.quiz.dto.QuizResponseDTO;
import com.quiz.dto.QuizAttemptDTO;
import com.quiz.model.Question;
import com.quiz.model.QuizQuestion;
import com.quiz.model.QuizResponse;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.QuizStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.sql.Timestamp;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attempts")
public class QuizAttemptController {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizResponseRepository quizResponseRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @GetMapping("/{userId}")
    public List<QuizAttemptDTO> getAttemptsByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return quizAttemptRepository.findByUserOrderByIdDesc(user)
                .stream()
                .map(QuizAttemptDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Start a new quiz attempt. This endpoint should be called when a user clicks "Start Quiz"
     */
    @PostMapping("/start")
    public ResponseEntity<?> startQuizAttempt(@RequestParam Long quizId, @RequestParam Long userId) {
        // Find the quiz and user
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check for existing attempts
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository.findByQuizAndUser(quiz, user);
        if (existingAttempt.isPresent()) {
            QuizAttempt previousAttempt = existingAttempt.get();
            // If there's a completed attempt, don't allow another one
            if (QuizStatus.COMPLETED.equals(previousAttempt.getStatus())) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You have already completed this quiz. Multiple attempts are not allowed.");
            }
            // If there's an in-progress attempt, return that instead of creating a new one
            if (QuizStatus.IN_PROGRESS.equals(previousAttempt.getStatus())) {
                return ResponseEntity.ok(new QuizAttemptDTO(previousAttempt));
            }
        }

        // Create new attempt
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStatus(QuizStatus.IN_PROGRESS);
        attempt.setStartTime(new Timestamp(System.currentTimeMillis()));
        attempt.setObtainedScore(0);
        attempt.setTotalScore(quiz.getTotalScore());

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        return ResponseEntity.ok(new QuizAttemptDTO(savedAttempt));
    }

    /**
     * This endpoint is deprecated. Use /start endpoint instead
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<?> createAttempt(@RequestBody QuizAttempt attempt) {
        return ResponseEntity
            .status(HttpStatus.GONE)
            .body("This endpoint is deprecated. Please use POST /api/attempts/start?quizId=X&userId=Y instead.");
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<QuizAttemptDTO> submitQuizAttempt(
            @PathVariable Long attemptId,
            @RequestBody QuizSubmissionRequest submission) {
        // Validate submission
        if (submission == null || submission.getResponses() == null || submission.getResponses().isEmpty()) {
            throw new IllegalArgumentException("Quiz submission must contain responses");
        }
        
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        // Check if quiz is already completed
        if (QuizStatus.COMPLETED.equals(attempt.getStatus())) {
            throw new IllegalStateException("This quiz attempt has already been submitted");
        }
        
        // Validate end time
        if (submission.getEndTime() == null || 
            submission.getEndTime().isBefore(attempt.getStartTime().toLocalDateTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Update attempt status and end time
        attempt.setStatus(QuizStatus.COMPLETED);
        attempt.setEndTime(submission.getEndTime());
        int obtainedScore = 0;
        
        // Save responses
        List<QuizResponse> responses = new ArrayList<>();
        for (QuizResponseDTO responseDTO : submission.getResponses()) {
            QuizResponse response = new QuizResponse();
            QuizQuestion quizQuestion = quizQuestionRepository.findById(responseDTO.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("QuizQuestion", "id", responseDTO.getQuestionId()));
            
            Question question = quizQuestion.getQuestion();
            
            response.setQuizAttempt(attempt);
            response.setQuestion(question);
            response.setUserOption(responseDTO.getUserOption());
            
            List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionIdAndCorrect(question.getId(), true);
            boolean isCorrect = false;
            for (QuestionOption correctOption : correctOptions) {
                if (correctOption.getOption().equals(responseDTO.getUserOption())) {
                    isCorrect = true;
                    break;
                }
            }
            response.setCorrect(isCorrect);
            int marks = isCorrect ? quizQuestion.getMarks() : 0;
            response.setMarks(marks);
            obtainedScore += marks;
            
            responses.add(response);
        }
        attempt.setObtainedScore(obtainedScore);
        
        // Save all responses
        quizResponseRepository.saveAll(responses);
        
        // Save the updated attempt
        QuizAttempt updatedAttempt = quizAttemptRepository.save(attempt);
        return ResponseEntity.ok(new QuizAttemptDTO(updatedAttempt));
    }
}