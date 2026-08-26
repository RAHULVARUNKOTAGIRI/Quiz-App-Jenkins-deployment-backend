package com.example.quizapp.entities;

import lombok.Data;

// { "questionId": 1, "selectedOption": "A" }
@Data
public class SubmitAnswerDTO {
    private Long questionId;
    private String selectedOption;
}
