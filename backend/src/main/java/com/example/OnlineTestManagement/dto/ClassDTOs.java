package com.example.OnlineTestManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class ClassDTOs {
    private ClassDTOs() {
    }

    public record ClassRequest(@NotBlank @Size(max = 150) String name) {
    }

    public record ClassResponse(Long id, String name, List<AuthDTOs.UserResponse> students) {
    }
}
