package com.example.OnlineTestManagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineTestManagement.dto.AuthDTOs;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/teacher/register")
    public AuthDTOs.AuthResponse registerTeacher(@Valid @RequestBody AuthDTOs.TeacherRegisterRequest req) {
        return authService.registerTeacher(req);
    }

    @PostMapping("/teacher/login")
    public AuthDTOs.AuthResponse loginTeacher(@Valid @RequestBody AuthDTOs.TeacherLoginRequest req) {
        return authService.loginTeacher(req);
    }

    @PostMapping("/student/login")
    public AuthDTOs.AuthResponse loginStudent(@Valid @RequestBody AuthDTOs.StudentLoginRequest req) {
        return authService.loginStudent(req);
    }

    @PostMapping("/change-password")
    public void changePassword(@AuthenticationPrincipal User user,
            @Valid @RequestBody AuthDTOs.ChangePasswordRequest req) {
        authService.changePassword(user, req);
    }

}
