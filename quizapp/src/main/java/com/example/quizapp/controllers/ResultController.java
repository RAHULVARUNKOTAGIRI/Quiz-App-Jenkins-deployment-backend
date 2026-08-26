package com.example.quizapp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quizapp.entities.QuizResult;
import com.example.quizapp.services.ResultService;

@RestController
@RequestMapping("/results")
public class ResultController {

    @Autowired
    ResultService rserv;

    // Every quiz attempt, newest first
    @GetMapping
    public List<QuizResult> getAllResults() {
        return rserv.getAllResults();
    }

    // Attempts for one subject
    @GetMapping("/subject/{subjectId}")
    public List<QuizResult> getResultsBySubject(@PathVariable Long subjectId) {
        return rserv.getResultsBySubject(subjectId);
    }

    // One attempt
    @GetMapping("/{resultId}")
    public QuizResult getResult(@PathVariable Long resultId) {
        return rserv.getResult(resultId);
    }

    @DeleteMapping("/{resultId}")
    public String deleteResult(@PathVariable Long resultId) {
        return rserv.deleteResult(resultId);
    }
}
