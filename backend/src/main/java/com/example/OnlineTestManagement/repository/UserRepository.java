package com.example.OnlineTestManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

import com.example.OnlineTestManagement.entity.Role;
import com.example.OnlineTestManagement.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findAllByRoleAndCreatedByTeacherId(Role role, Long teacherId);

    boolean existsByIdAndRoleAndCreatedByTeacherId(Long id, Role role, Long teacherId);
}
