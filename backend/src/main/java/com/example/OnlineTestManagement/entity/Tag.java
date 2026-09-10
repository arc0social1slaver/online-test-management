package com.example.OnlineTestManagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(name = "uk_tags_name", columnNames = "name"))
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id")
    private User createdByTeacher;

    public Tag() {
    }

    public Tag(String name, User createdByTeacher) {
        this.name = name;
        this.createdByTeacher = createdByTeacher;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getCreatedByTeacher() {
        return createdByTeacher;
    }

    public void setCreatedByTeacher(User createdByTeacher) {
        this.createdByTeacher = createdByTeacher;
    }
}
