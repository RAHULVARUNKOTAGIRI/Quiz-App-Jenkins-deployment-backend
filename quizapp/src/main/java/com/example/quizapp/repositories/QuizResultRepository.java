package com.example.quizapp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quizapp.entities.QuizResult;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findAllByOrderByAttemptedAtDesc();

    List<QuizResult> findAllBySubjectIdOrderByAttemptedAtDesc(Long subjectId);
}
