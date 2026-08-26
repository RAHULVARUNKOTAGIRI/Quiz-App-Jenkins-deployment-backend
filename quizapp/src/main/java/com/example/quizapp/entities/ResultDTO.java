package com.example.quizapp.entities;

import java.util.List;

import lombok.Data;

// The score the student sees immediately after submitting.
@Data
public class ResultDTO {
    private Long resultId; // id of the saved attempt, look it up later via /results/{id}
    private Long subjectId;
    private String subjectName;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int unanswered;
    private int score; // percentage
    private List<AnswerReviewDTO> review;

    public ResultDTO(Subject subject, int totalQuestions, int correctAnswers, int wrongAnswers,
            int unanswered, List<AnswerReviewDTO> review) {
        this.subjectId = subject.getId();
        this.subjectName = subject.getName();
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unanswered = unanswered;
        this.score = totalQuestions == 0 ? 0 : Math.round((correctAnswers * 100f) / totalQuestions);
        this.review = review;
    }
}
