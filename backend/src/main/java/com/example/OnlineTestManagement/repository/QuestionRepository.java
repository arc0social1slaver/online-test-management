package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.OnlineTestManagement.entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("select q from Question q where q.id = :id and q.teacher.id = :teacherId and q.active = true")
    Optional<Question> findByIdAndTeacherId(@Param("id") Long id, @Param("teacherId") Long teacherId);

    @Query("select q from Question q where q.teacher.id = :teacherId and q.active = true order by q.id desc")
    List<Question> findAllByTeacherIdOrderByIdDesc(@Param("teacherId") Long teacherId);

    @Query("select distinct q from Question q join q.tags t where q.teacher.id = :teacherId and q.active = true and t.id in :tagIds")
    List<Question> findDistinctByTeacherAndTagIds(@Param("teacherId") Long teacherId,
            @Param("tagIds") List<Long> tagIds);
}
