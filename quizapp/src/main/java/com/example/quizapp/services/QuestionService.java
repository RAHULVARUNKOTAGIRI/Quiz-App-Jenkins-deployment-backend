package com.example.quizapp.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quizapp.entities.AnswerReviewDTO;
import com.example.quizapp.entities.Question;
import com.example.quizapp.entities.QuestionDTO;
import com.example.quizapp.entities.ResultDTO;
import com.example.quizapp.entities.Subject;
import com.example.quizapp.entities.SubmitAnswerDTO;
import com.example.quizapp.entities.SubmitQuizDTO;
import com.example.quizapp.exceptions.BadRequestException;
import com.example.quizapp.exceptions.ResourceNotFoundException;
import com.example.quizapp.repositories.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    QuestionRepository qrepo;

    @Autowired
    SubjectService sserv;

    @Autowired
    ResultService rserv;

    public Question addQuestion(Long subjectId, Question question) {
        Subject subject = sserv.findSubjectById(subjectId);
        question.setId(null);
        question.setSubject(subject);
        question.setCorrectOption(normaliseOption(question.getCorrectOption()));
        return qrepo.save(question);
    }

    // Admin can paste in a whole set of questions at once.
    public List<Question> addQuestions(Long subjectId, List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new BadRequestException("No questions supplied");
        }
        return questions.stream()
                .map(question -> addQuestion(subjectId, question))
                .collect(Collectors.toList());
    }

    // Admin view - includes the correct answer
    public List<Question> getQuestionsForAdmin(Long subjectId) {
        return qrepo.findAllBySubject(sserv.findSubjectById(subjectId));
    }

    // Student view - correct answer hidden
    public List<QuestionDTO> getQuestionsForStudent(Long subjectId) {
        Subject subject = sserv.findSubjectById(subjectId);
        List<Question> questions = qrepo.findAllBySubject(subject);
        if (questions.isEmpty()) {
            throw new BadRequestException("No questions added yet for " + subject.getName());
        }
        return questions.stream().map(QuestionDTO::new).collect(Collectors.toList());
    }

    public Question updateQuestion(Long questionId, Question updated) {
        Question question = findQuestionById(questionId);
        question.setQuestionText(updated.getQuestionText());
        question.setOptionA(updated.getOptionA());
        question.setOptionB(updated.getOptionB());
        question.setOptionC(updated.getOptionC());
        question.setOptionD(updated.getOptionD());
        question.setCorrectOption(normaliseOption(updated.getCorrectOption()));
        return qrepo.save(question);
    }

    public String deleteQuestion(Long questionId) {
        qrepo.delete(findQuestionById(questionId));
        return "Question deleted";
    }

    public long countQuestions(Long subjectId) {
        return qrepo.countBySubject(sserv.findSubjectById(subjectId));
    }

    /*
     * Student clicks Submit and gets the score back straight away.
     * Every question in the subject counts towards the total, so skipping
     * questions cannot inflate the percentage.
     */
    public ResultDTO submitQuiz(SubmitQuizDTO submission) {
        if (submission.getSubjectId() == null) {
            throw new BadRequestException("subjectId is required");
        }
        Subject subject = sserv.findSubjectById(submission.getSubjectId());
        List<Question> questions = qrepo.findAllBySubject(subject);
        if (questions.isEmpty()) {
            throw new BadRequestException("No questions added yet for " + subject.getName());
        }

        Map<Long, String> chosen = new HashMap<>();
        if (submission.getAnswers() != null) {
            for (SubmitAnswerDTO answer : submission.getAnswers()) {
                if (answer.getQuestionId() != null) {
                    chosen.put(answer.getQuestionId(), answer.getSelectedOption());
                }
            }
        }

        int correct = 0;
        int wrong = 0;
        int unanswered = 0;
        List<AnswerReviewDTO> review = new ArrayList<>();

        for (Question question : questions) {
            String selected = chosen.get(question.getId());
            boolean answered = selected != null && !selected.isBlank();
            boolean isCorrect = answered && selected.trim().equalsIgnoreCase(question.getCorrectOption());
            if (!answered) {
                unanswered++;
            } else if (isCorrect) {
                correct++;
            } else {
                wrong++;
            }
            review.add(new AnswerReviewDTO(question, answered ? selected.trim().toUpperCase() : null, isCorrect));
        }

        ResultDTO result = new ResultDTO(subject, questions.size(), correct, wrong, unanswered, review);
        // Every submission is stored so the admin can pull them back from /results.
        result.setResultId(rserv.save(subject, submission.getStudentName(), result).getId());
        return result;
    }

    // correctOption drives the scoring, so it is checked here as well
    // (a bulk upload does not go through the @Valid on a single question body).
    private String normaliseOption(String option) {
        if (option == null || !option.trim().matches("(?i)[ABCD]")) {
            throw new BadRequestException("correctOption must be A, B, C or D");
        }
        return option.trim().toUpperCase();
    }

    private Question findQuestionById(Long questionId) {
        return qrepo.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }
}
