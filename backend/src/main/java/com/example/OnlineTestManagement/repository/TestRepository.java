package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineTestManagement.entity.Test;

import java.util.List;
import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findAllByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    Optional<Test> findByIdAndTeacherId(Long id, Long teacherId);
}
