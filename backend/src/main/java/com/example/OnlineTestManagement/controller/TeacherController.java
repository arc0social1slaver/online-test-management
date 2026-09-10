package com.example.OnlineTestManagement.controller;

import com.example.OnlineTestManagement.dto.AuthDTOs;
import com.example.OnlineTestManagement.dto.ClassDTOs;
import com.example.OnlineTestManagement.dto.QuestionDTOs;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.service.AuthService;
import com.example.OnlineTestManagement.service.ClassRoomService;
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
    private final ClassRoomService classRoomService;

    public TeacherController(AuthService authService, TagService tagService, QuestionService questionService,
            ClassRoomService classRoomService) {
        this.authService = authService;
        this.tagService = tagService;
        this.questionService = questionService;
        this.classRoomService = classRoomService;
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

    @GetMapping("/classes")
    public List<ClassDTOs.ClassResponse> getAllClasses(@AuthenticationPrincipal User teacher) {
        return classRoomService.list(teacher);
    }

    @GetMapping("/classes/{id}")
    public ClassDTOs.ClassResponse getClass(@AuthenticationPrincipal User teacher, Long id) {
        return classRoomService.get(teacher, id);
    }

    @PostMapping("/classes")
    public ClassDTOs.ClassResponse createClass(@AuthenticationPrincipal User teacher,
            @Valid @RequestBody ClassDTOs.ClassRequest classRequest) {
        return classRoomService.create(teacher, classRequest);
    }

    @PutMapping("/classes/{id}")
    public ClassDTOs.ClassResponse updateClass(@AuthenticationPrincipal User teacher, @PathVariable Long id,
            @Valid @RequestBody ClassDTOs.ClassRequest classRequest) {
        return classRoomService.update(teacher, id, classRequest);
    }

    @DeleteMapping("/classes/{id}")
    public void deleteClass(@AuthenticationPrincipal User teacher, @PathVariable Long id) {
        classRoomService.delete(teacher, id);
    }

    @PostMapping("/classes/{classId}/students/{studentId}")
    public ClassDTOs.ClassResponse addStudent(@AuthenticationPrincipal User teacher, @PathVariable Long classId,
            @PathVariable Long studentId) {
        return classRoomService.addStudent(teacher, classId, studentId);
    }

    @DeleteMapping("/classes/{classId}/students/{studentId}")
    public ClassDTOs.ClassResponse removeStudent(@AuthenticationPrincipal User teacher, @PathVariable Long classId,
            @PathVariable Long studentId) {
        return classRoomService.removeStudent(teacher, classId, studentId);
    }
}
