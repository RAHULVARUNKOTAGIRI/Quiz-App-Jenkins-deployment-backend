package com.example.quizapp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quizapp.entities.Subject;
import com.example.quizapp.services.SubjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    @Autowired
    SubjectService sserv;

    // Admin creates a new subject e.g. Maths, Science, History
    @PostMapping
    public Subject createSubject(@Valid @RequestBody Subject subject) {
        return sserv.createSubject(subject);
    }

    // Student (or admin) sees the list of subjects to choose from
    @GetMapping
    public List<Subject> getAllSubjects() {
        return sserv.getAllSubjects();
    }

    @GetMapping("/{subjectId}")
    public Subject getSubject(@PathVariable Long subjectId) {
        return sserv.findSubjectById(subjectId);
    }

    @DeleteMapping("/{subjectId}")
    public String deleteSubject(@PathVariable Long subjectId) {
        return sserv.deleteSubject(subjectId);
    }
}
