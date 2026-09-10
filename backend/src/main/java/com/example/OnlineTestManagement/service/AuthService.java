package com.example.OnlineTestManagement.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.AuthDTOs;
import com.example.OnlineTestManagement.entity.Role;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.exception.ApiException;
import com.example.OnlineTestManagement.repository.UserRepository;
import com.example.OnlineTestManagement.security.JwtService;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDTOs.AuthResponse registerTeacher(AuthDTOs.TeacherRegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Teacher's email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setRole(Role.TEACHER);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user = userRepository.save(user);
        return new AuthDTOs.AuthResponse(user.getId(), user.getRole().name(),
                jwtService.generateToken(user.getId(), user.getRole().name()));
    }

    public AuthDTOs.AuthResponse loginTeacher(AuthDTOs.TeacherLoginRequest req) {
        User teacher = userRepository.findByEmailIgnoreCase(req.email().trim())
                .filter(elem -> elem.getRole() == Role.TEACHER)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Credential"));
        return login(teacher, req.password());
    }

    public AuthDTOs.AuthResponse loginStudent(AuthDTOs.StudentLoginRequest req) {
        User student = userRepository.findByUsernameIgnoreCase(req.username().trim())
                .filter(elem -> elem.getRole() == Role.STUDENT)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Credential"));
        return login(student, req.password());
    }

    @Transactional
    public AuthDTOs.UserResponse registerStudent(User teacher, AuthDTOs.CreateStudentRequest req) {
        String username = req.username().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Teacher's email already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setRole(Role.STUDENT);
        user.setCreatedByTeacher(teacher);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void changePassword(User user, AuthDTOs.ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    public AuthDTOs.AuthResponse login(User user, String pwd) {
        if (!passwordEncoder.matches(pwd, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthDTOs.AuthResponse(user.getId(), user.getRole().name(),
                jwtService.generateToken(user.getId(), user.getRole().name()));
    }

    public AuthDTOs.UserResponse toResponse(User user) {
        return new AuthDTOs.UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
    }
}
