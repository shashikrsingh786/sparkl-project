package com.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QuizSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuizSystemApplication.class, args);
    }
} 