package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineTestManagement.entity.ClassRoom;

import java.util.List;
import java.util.Optional;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    List<ClassRoom> findAllByTeacherIdOrderByIdDesc(Long teacherId);

    Optional<ClassRoom> findByIdAndTeacherId(Long id, Long teacherId);
}
