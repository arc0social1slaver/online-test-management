package com.example.OnlineTestManagement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineTestManagement.dto.TestDTOs;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.service.TestService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final TestService testService;

    public StudentController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/tests/{testId}")
    public TestDTOs.TakeTestResponse openTest(@AuthenticationPrincipal User student, @PathVariable Long testId) {
        return testService.openTest(student, testId);
    }

    @PostMapping("/test/{testId}/submit")
    public TestDTOs.ResultDetail submitTest(@AuthenticationPrincipal User student, @PathVariable Long testId,
            @Valid @RequestBody TestDTOs.SubmitTestRequest submitTestRequest) {
        return testService.submitTest(student, testId, submitTestRequest);
    }
}
