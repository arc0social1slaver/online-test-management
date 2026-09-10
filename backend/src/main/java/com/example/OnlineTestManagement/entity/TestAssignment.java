package com.example.OnlineTestManagement.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "test_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_test_target", columnNames = {
        "test_id", "class_id", "student_id" }))
public class TestAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassRoom classRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt;

    @PrePersist
    void prePersist() {
        assignedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
