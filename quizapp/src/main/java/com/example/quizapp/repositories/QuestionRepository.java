package com.example.quizapp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quizapp.entities.Question;
import com.example.quizapp.entities.Subject;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findAllBySubject(Subject subject);

    long countBySubject(Subject subject);
}
