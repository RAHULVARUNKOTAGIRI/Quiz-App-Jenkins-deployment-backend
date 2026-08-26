package com.example.quizapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Set from the URL by the service, never from the request body.
    @ManyToOne
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({ "questions" })
    private Subject subject;

    @NotBlank(message = "questionText is required")
    @Column(length = 1000)
    private String questionText;

    @NotBlank(message = "optionA is required")
    private String optionA;

    @NotBlank(message = "optionB is required")
    private String optionB;

    private String optionC;
    private String optionD;

    // stores "A", "B", "C" or "D"
    @Pattern(regexp = "(?i)[ABCD]", message = "correctOption must be A, B, C or D")
    private String correctOption;
}
