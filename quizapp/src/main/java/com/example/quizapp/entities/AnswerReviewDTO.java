package com.example.quizapp.entities;

import lombok.Data;

// Per question breakdown returned with the score.
@Data
public class AnswerReviewDTO {
    private Long questionId;
    private String questionText;
    private String selectedOption;
    private String correctOption;
    private boolean correct;

    public AnswerReviewDTO(Question question, String selectedOption, boolean correct) {
        this.questionId = question.getId();
        this.questionText = question.getQuestionText();
        this.selectedOption = selectedOption;
        this.correctOption = question.getCorrectOption();
        this.correct = correct;
    }
}
