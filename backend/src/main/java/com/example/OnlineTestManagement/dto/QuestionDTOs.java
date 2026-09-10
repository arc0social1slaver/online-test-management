package com.example.OnlineTestManagement.dto;

import java.util.List;

import jakarta.validation.constraints.*;

public final class QuestionDTOs {
    private QuestionDTOs() {
    }

    public record QuestionRequest(
            @NotBlank String text,
            @NotBlank String choiceA,
            @NotBlank String choiceB,
            @NotBlank String choiceC,
            @NotBlank String choiceD,
            @NotNull @Min(0) @Max(3) Integer correctIndex,
            @NotEmpty List<@NotNull Long> tagIds) {
    }

    public record QuestionResponse(
            Long id, String text, List<String> choices, Integer correctIndex, List<TagResponse> tags) {
    }

    public record TagRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record TagResponse(Long id, String name) {
    }
}
