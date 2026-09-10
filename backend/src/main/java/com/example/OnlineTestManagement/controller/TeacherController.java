package com.example.OnlineTestManagement.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineTestManagement.dto.AuthDTOs;
import com.example.OnlineTestManagement.dto.QuestionDTOs;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.service.AuthService;
import com.example.OnlineTestManagement.service.QuestionService;
import com.example.OnlineTestManagement.service.TagService;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {
    private final AuthService authService;
    private final TagService tagService;
    private final QuestionService questionService;

    public TeacherController(AuthService authService, TagService tagService, QuestionService questionService) {
        this.authService = authService;
        this.tagService = tagService;
        this.questionService = questionService;
    }

    @PostMapping("/students")
    public AuthDTOs.UserResponse createStudent(@AuthenticationPrincipal User teacher,
            @Valid @RequestBody AuthDTOs.CreateStudentRequest req) {
        return authService.registerStudent(teacher, req);
    }

    @GetMapping("/tags")
    public List<QuestionDTOs.TagResponse> listTags() {
        return tagService.list();
    }

    @PostMapping("/tags")
    public QuestionDTOs.TagResponse createTag(@AuthenticationPrincipal User teacher,
            @Valid @RequestBody QuestionDTOs.TagRequest tagRequest) {
        return tagService.create(teacher, tagRequest);
    }

    @GetMapping("/questions")
    public List<QuestionDTOs.QuestionResponse> listQuestions(@AuthenticationPrincipal User teacher) {
        return questionService.list(teacher);
    }

    @PostMapping("/questions")
    public QuestionDTOs.QuestionResponse createQuestion(@AuthenticationPrincipal User teacher,
            @Valid @RequestBody QuestionDTOs.QuestionRequest questionRequest) {
        return questionService.create(teacher, questionRequest);
    }

    @PutMapping("/questions/{id}")
    public QuestionDTOs.QuestionResponse updateQuestion(@AuthenticationPrincipal User teacher, @PathVariable Long id,
            @Valid @RequestBody QuestionDTOs.QuestionRequest questionRequest) {
        return questionService.update(teacher, id, questionRequest);
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@AuthenticationPrincipal User teacher, @PathVariable Long id) {
        questionService.delete(teacher, id);
    }
}
