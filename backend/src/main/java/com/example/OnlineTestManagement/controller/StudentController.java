package com.example.OnlineTestManagement.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.OnlineTestManagement.dto.TestDTOs;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.service.TestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final TestService testService;

    public StudentController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/tests/available")
    public List<TestDTOs.TestSummary> available(@AuthenticationPrincipal User student) {
        return testService.studAvailableTest(student);
    }

    @GetMapping("/tests/completed")
    public List<TestDTOs.TestSummary> completed(@AuthenticationPrincipal User student) {
        return testService.studCompletedTest(student);
    }

    @GetMapping("/tests/{testId}")
    public TestDTOs.TakeTestResponse openTest(@AuthenticationPrincipal User student, @PathVariable Long testId) {
        return testService.openTest(student, testId);
    }

    @PostMapping("/tests/{testId}/submit")
    public TestDTOs.ResultDetail submitTest(@AuthenticationPrincipal User student, @PathVariable Long testId,
            @Valid @RequestBody TestDTOs.SubmitTestRequest submitTestRequest) {
        return testService.submitTest(student, testId, submitTestRequest);
    }

    @GetMapping("/results")
    public List<TestDTOs.ResultSummary> listResults(@AuthenticationPrincipal User student) {
        return testService.studentResults(student);
    }

    @GetMapping("/results/{resultId}")
    public TestDTOs.ResultDetail getResult(@AuthenticationPrincipal User student, @PathVariable Long resultId) {
        return testService.studentResult(student, resultId);
    }
}
