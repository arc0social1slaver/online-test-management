package com.example.OnlineTestManagement.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineTestManagement.dto.AuthDTOs;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.service.AuthService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {
    private final AuthService authService;

    public TeacherController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/students")
    public AuthDTOs.UserResponse createStudent(@AuthenticationPrincipal User teacher,
            @Valid @RequestBody AuthDTOs.CreateStudentRequest req) {
        return authService.registerStudent(teacher, req);
    }
}
