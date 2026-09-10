package com.example.OnlineTestManagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDTOs {
    public record TeacherRegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record TeacherLoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record StudentLoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record CreateStudentRequest(
            @NotBlank @Size(min = 3, max = 100) String username,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record AuthResponse(Long userId, String role, String token) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 100) String newPassword) {
    }

    public record UserResponse(Long id, String email, String username) {
    }
}
