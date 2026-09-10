package com.example.OnlineTestManagement.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.*;

public final class TestDTOs {
    private TestDTOs() {
    }

    public record GenerateTestRequest(
            @NotBlank @Size(max = 200) String title,
            @NotEmpty List<@NotNull Long> tagIds,
            @NotNull @Min(1) Integer numberOfQuestions) {
    }

    public record AssignTestRequest(Long classId, Long studentId) {
        @AssertTrue(message = "Exactly one of classId or studentId must be provided")
        public boolean hasExactlyOneTarget() {
            return (classId == null) ^ (studentId == null);
        }
    }

    public record TestSummary(Long id, String title, int numberOfQuestions, Instant createdAt) {
    }

}
