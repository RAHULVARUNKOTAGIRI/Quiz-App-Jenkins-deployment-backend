package com.example.quizapp.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quizapp.entities.QuizResult;
import com.example.quizapp.entities.ResultDTO;
import com.example.quizapp.entities.Subject;
import com.example.quizapp.exceptions.ResourceNotFoundException;
import com.example.quizapp.repositories.QuizResultRepository;

@Service
public class ResultService {

    @Autowired
    QuizResultRepository rrepo;

    @Autowired
    SubjectService sserv;

    // Called by QuestionService the moment a quiz is submitted.
    public QuizResult save(Subject subject, String studentName, ResultDTO result) {
        QuizResult saved = new QuizResult();
        saved.setSubject(subject);
        saved.setStudentName(studentName == null || studentName.isBlank() ? "Student" : studentName.trim());
        saved.setTotalQuestions(result.getTotalQuestions());
        saved.setCorrectAnswers(result.getCorrectAnswers());
        saved.setWrongAnswers(result.getWrongAnswers());
        saved.setUnanswered(result.getUnanswered());
        saved.setScore(result.getScore());
        return rrepo.save(saved);
    }

    // Every attempt, newest first.
    public List<QuizResult> getAllResults() {
        return rrepo.findAllByOrderByAttemptedAtDesc();
    }

    // Attempts for one subject only.
    public List<QuizResult> getResultsBySubject(Long subjectId) {
        sserv.findSubjectById(subjectId);
        return rrepo.findAllBySubjectIdOrderByAttemptedAtDesc(subjectId);
    }

    public QuizResult getResult(Long resultId) {
        return rrepo.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id " + resultId));
    }

    public String deleteResult(Long resultId) {
        rrepo.delete(getResult(resultId));
        return "Result deleted";
    }
}
