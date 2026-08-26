package com.example.quizapp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quizapp.entities.Question;
import com.example.quizapp.entities.QuestionDTO;
import com.example.quizapp.entities.ResultDTO;
import com.example.quizapp.entities.SubmitQuizDTO;
import com.example.quizapp.services.QuestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    QuestionService qserv;

    // Admin adds one question under a subject
    @PostMapping("/subject/{subjectId}")
    public Question addQuestion(@PathVariable Long subjectId, @Valid @RequestBody Question question) {
        return qserv.addQuestion(subjectId, question);
    }

    // Admin adds several questions at once
    @PostMapping("/subject/{subjectId}/bulk")
    public List<Question> addQuestions(@PathVariable Long subjectId, @Valid @RequestBody List<Question> questions) {
        return qserv.addQuestions(subjectId, questions);
    }

    // Admin view of all questions in a subject, correct answer included
    @GetMapping("/subject/{subjectId}/admin")
    public List<Question> getQuestionsForAdmin(@PathVariable Long subjectId) {
        return qserv.getQuestionsForAdmin(subjectId);
    }

    // Student picks a subject and gets its questions, correct answer hidden
    @GetMapping("/subject/{subjectId}")
    public List<QuestionDTO> getQuestionsForStudent(@PathVariable Long subjectId) {
        return qserv.getQuestionsForStudent(subjectId);
    }

    @PutMapping("/{questionId}")
    public Question updateQuestion(@PathVariable Long questionId, @Valid @RequestBody Question question) {
        return qserv.updateQuestion(questionId, question);
    }

    @DeleteMapping("/{questionId}")
    public String deleteQuestion(@PathVariable Long questionId) {
        return qserv.deleteQuestion(questionId);
    }

    @GetMapping("/subject/{subjectId}/count")
    public long countQuestions(@PathVariable Long subjectId) {
        return qserv.countQuestions(subjectId);
    }

    // Student clicks Submit and gets the score back in the response
    @PostMapping("/submit")
    public ResultDTO submitQuiz(@RequestBody SubmitQuizDTO submission) {
        return qserv.submitQuiz(submission);
    }
}
