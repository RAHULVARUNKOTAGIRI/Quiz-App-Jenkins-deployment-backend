package com.example.quizapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.quizapp.entities.Question;
import com.example.quizapp.entities.QuestionDTO;
import com.example.quizapp.entities.QuizResult;
import com.example.quizapp.entities.ResultDTO;
import com.example.quizapp.entities.Subject;
import com.example.quizapp.entities.SubmitAnswerDTO;
import com.example.quizapp.entities.SubmitQuizDTO;
import com.example.quizapp.exceptions.BadRequestException;
import com.example.quizapp.services.QuestionService;
import com.example.quizapp.services.ResultService;
import com.example.quizapp.services.SubjectService;

// Admin creates a subject with questions -> student answers -> score comes back
// and the attempt shows up in the results API.
@SpringBootTest
class QuizFlowTests {

    @Autowired
    SubjectService sserv;

    @Autowired
    QuestionService qserv;

    @Autowired
    ResultService rserv;

    @Test
    void studentGetsScoreAfterSubmitting() {
        Subject maths = new Subject();
        maths.setName("Maths-" + System.nanoTime());
        Long subjectId = sserv.createSubject(maths).getId();

        qserv.addQuestion(subjectId, question("2 + 2 = ?", "3", "4", "5", "6", "B"));
        qserv.addQuestion(subjectId, question("5 x 2 = ?", "10", "7", "12", "9", "A"));
        qserv.addQuestion(subjectId, question("9 - 4 = ?", "3", "4", "5", "6", "C"));

        List<QuestionDTO> paper = qserv.getQuestionsForStudent(subjectId);
        assertEquals(3, paper.size());

        SubmitQuizDTO submission = new SubmitQuizDTO();
        submission.setSubjectId(subjectId);
        submission.setStudentName("Ravi");
        // first correct, second wrong, third left blank
        submission.setAnswers(List.of(
                answer(paper.get(0).getId(), "b"),
                answer(paper.get(1).getId(), "D")));

        ResultDTO result = qserv.submitQuiz(submission);

        assertEquals(3, result.getTotalQuestions());
        assertEquals(1, result.getCorrectAnswers());
        assertEquals(1, result.getWrongAnswers());
        assertEquals(1, result.getUnanswered());
        assertEquals(33, result.getScore());
        assertTrue(result.getReview().get(0).isCorrect());

        // the attempt is stored and readable through the results API
        assertNotNull(result.getResultId());
        QuizResult saved = rserv.getResult(result.getResultId());
        assertEquals("Ravi", saved.getStudentName());
        assertEquals(33, saved.getScore());
        assertEquals(1, saved.getCorrectAnswers());
        assertEquals(1, rserv.getResultsBySubject(subjectId).size());
        assertTrue(rserv.getAllResults().stream()
                .anyMatch(r -> r.getId().equals(result.getResultId())));
    }

    @Test
    void studentNameIsOptional() {
        Subject subject = new Subject();
        subject.setName("History-" + System.nanoTime());
        Long subjectId = sserv.createSubject(subject).getId();
        qserv.addQuestion(subjectId, question("Taj Mahal city?", "Agra", "Delhi", null, null, "A"));

        SubmitQuizDTO submission = new SubmitQuizDTO();
        submission.setSubjectId(subjectId);

        ResultDTO result = qserv.submitQuiz(submission);

        assertEquals(1, result.getUnanswered());
        assertEquals(0, result.getScore());
        assertEquals("Student", rserv.getResult(result.getResultId()).getStudentName());
    }

    @Test
    void badCorrectOptionIsRejected() {
        Subject subject = new Subject();
        subject.setName("Science-" + System.nanoTime());
        Long subjectId = sserv.createSubject(subject).getId();
        assertThrows(BadRequestException.class,
                () -> qserv.addQuestion(subjectId, question("Sky colour?", "Blue", "Red", null, null, "Z")));
    }

    private Question question(String text, String a, String b, String c, String d, String correct) {
        Question question = new Question();
        question.setQuestionText(text);
        question.setOptionA(a);
        question.setOptionB(b);
        question.setOptionC(c);
        question.setOptionD(d);
        question.setCorrectOption(correct);
        return question;
    }

    private SubmitAnswerDTO answer(Long questionId, String option) {
        SubmitAnswerDTO answer = new SubmitAnswerDTO();
        answer.setQuestionId(questionId);
        answer.setSelectedOption(option);
        return answer;
    }
}
