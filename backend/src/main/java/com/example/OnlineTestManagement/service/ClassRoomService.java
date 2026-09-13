package com.example.OnlineTestManagement.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.ClassDTOs;
import com.example.OnlineTestManagement.entity.ClassRoom;
import com.example.OnlineTestManagement.entity.Role;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.exception.ApiException;
import com.example.OnlineTestManagement.repository.ClassRoomRepository;
import com.example.OnlineTestManagement.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassRoomService {
    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public ClassRoomService(ClassRoomRepository classRoomRepository, UserRepository userRepository,
            AuthService authService) {
        this.classRoomRepository = classRoomRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public List<ClassDTOs.ClassResponse> list(User teacher) {
        return classRoomRepository.findAllByTeacherIdOrderByIdDesc(teacher.getId()).stream().map(this::toResponse)
                .toList();
    }

    public ClassDTOs.ClassResponse get(User teacher, Long id) {
        ClassRoom cl = classRoomRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        return toResponse(cl);
    }

    @Transactional
    public ClassDTOs.ClassResponse create(User teacher, ClassDTOs.ClassRequest classRequest) {
        ClassRoom cl = new ClassRoom();
        cl.setName(classRequest.name().trim());
        cl.setTeacher(teacher);
        cl = classRoomRepository.save(cl);
        return toResponse(cl);
    }

    @Transactional
    public ClassDTOs.ClassResponse update(User teacher, Long id, ClassDTOs.ClassRequest classRequest) {
        ClassRoom cl = classRoomRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        cl.setName(classRequest.name().trim());
        cl = classRoomRepository.save(cl);
        return toResponse(cl);
    }

    @Transactional
    public void delete(User teacher, Long id) {
        ClassRoom cl = classRoomRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        classRoomRepository.delete(cl);
    }

    @Transactional
    public ClassDTOs.ClassResponse addStudent(User teacher, Long id, Long studentId) {
        ClassRoom cl = classRoomRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        User student = userRepository.findById(studentId)
                .filter(u -> u.getRole() == Role.STUDENT && teacher.getId().equals(u.getCreatedByTeacher().getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        cl.getStudents().add(student);
        cl = classRoomRepository.save(cl);
        return toResponse(cl);
    }

    @Transactional
    public ClassDTOs.ClassResponse removeStudent(User teacher, Long id, Long studentId) {
        ClassRoom cl = classRoomRepository.findByIdAndTeacherId(id, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Classroom not found"));
        cl.getStudents().removeIf(e -> e.getId().equals(studentId));
        cl = classRoomRepository.save(cl);
        return toResponse(cl);
    }

    public ClassDTOs.ClassResponse toResponse(ClassRoom cl) {
        return new ClassDTOs.ClassResponse(cl.getId(), cl.getName(),
                cl.getStudents().stream().map(authService::toResponse).toList());
    }
}
