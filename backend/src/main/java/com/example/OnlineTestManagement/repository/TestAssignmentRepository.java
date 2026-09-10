package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.OnlineTestManagement.entity.TestAssignment;

import java.util.List;

public interface TestAssignmentRepository extends JpaRepository<TestAssignment, Long> {
    boolean existsByTestIdAndStudentId(Long testId, Long studentId);

    List<TestAssignment> findAllByTestTeacherIdOrderByAssignedAtDesc(Long teacherId);

    @Query("select distinct a.test.id from TestAssignment a left join a.classRoom c left join c.students s where a.student.id = :studentId or s.id = :studentId")
    List<Long> findAccessibleTestIdsForStudent(@Param("studentId") Long studentId);
}
