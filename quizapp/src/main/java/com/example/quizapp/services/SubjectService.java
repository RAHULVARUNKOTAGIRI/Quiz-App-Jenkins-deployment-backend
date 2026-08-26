package com.example.quizapp.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.quizapp.entities.Subject;
import com.example.quizapp.exceptions.BadRequestException;
import com.example.quizapp.exceptions.ResourceNotFoundException;
import com.example.quizapp.repositories.SubjectRepository;

@Service
public class SubjectService {

    @Autowired
    SubjectRepository srepo;

    public Subject createSubject(Subject subject) {
        String name = subject.getName().trim();
        srepo.findByNameIgnoreCase(name).ifPresent(existing -> {
            throw new BadRequestException("Subject '" + name + "' already exists");
        });
        subject.setName(name);
        return srepo.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return srepo.findAll();
    }

    public Subject findSubjectById(Long id) {
        return srepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
    }

    public String deleteSubject(Long id) {
        Subject subject = findSubjectById(id);
        srepo.delete(subject);
        return "Subject deleted";
    }
}
