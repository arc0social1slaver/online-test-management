package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineTestManagement.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findAllByOrderByNameAsc();
}
