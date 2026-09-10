package com.example.OnlineTestManagement.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
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

    public record TakeTestResponse(Long id, String title, List<TakeQuestion> questions) {
    }

    public record TakeQuestion(Long questionId, String text, List<String> choices) {
    }

    public record TestSummary(Long id, String title, int numberOfQuestions, Instant createdAt) {
    }

    public record ResultDetail(Long resultId, Long testId, String testTitle,
            Long studentId, String studentUsername, Integer score,
            Integer totalQuestions, Instant submittedAt,
            List<ResultAnswer> answers) {
    }

    public record ResultAnswer(Long questionId, String questionText, List<String> choices,
            Integer studentAnswerIndex, Integer correctAnswerIndex) {
    }

    public record AnswerSubmission(Long questionId, @Min(0) @Max(3) Integer answerIndex) {
    }

    public record SubmitTestRequest(@NotNull List<@Valid AnswerSubmission> answers) {
    }

}
