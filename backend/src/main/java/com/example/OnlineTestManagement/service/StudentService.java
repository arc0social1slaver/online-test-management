package com.example.OnlineTestManagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.AuthDTOs;
import com.example.OnlineTestManagement.entity.Role;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.repository.UserRepository;

@Service
public class StudentService {
    private final UserRepository userRepository;
    private final AuthService authService;

    public StudentService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public List<AuthDTOs.UserResponse> getOwnedStudents(User teacher) {
        return userRepository.findAllByRoleAndCreatedByTeacherId(Role.STUDENT, teacher.getId()).stream()
                .map(authService::toResponse).toList();
    }
}
