package com.example.quizapp.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

// What the student POSTs when clicking Submit:
// { "subjectId": 1, "studentName": "Ravi", "answers": [ { "questionId": 1, "selectedOption": "A" } ] }
// studentName is optional - it is only used to label the saved result.
@Data
public class SubmitQuizDTO {
    private Long subjectId;
    private String studentName;
    private List<SubmitAnswerDTO> answers = new ArrayList<>();
}
