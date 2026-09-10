package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineTestManagement.entity.TestAttempt;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    Optional<TestAttempt> findByTestIdAndStudentId(Long testId, Long studentId);

    List<TestAttempt> findAllByStudentIdOrderBySubmittedAtDesc(Long studentId);

    List<TestAttempt> findAllByTestTeacherIdOrderBySubmittedAtDesc(Long teacherId);

    Optional<TestAttempt> findByIdAndStudentId(Long id, Long studentId);

    Optional<TestAttempt> findByIdAndTestTeacherId(Long id, Long teacherId);
}
