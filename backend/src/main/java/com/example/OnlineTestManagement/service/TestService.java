package com.example.OnlineTestManagement.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.TestDTOs;
import com.example.OnlineTestManagement.entity.ClassRoom;
import com.example.OnlineTestManagement.entity.Question;
import com.example.OnlineTestManagement.entity.Role;
import com.example.OnlineTestManagement.entity.Tag;
import com.example.OnlineTestManagement.entity.Test;
import com.example.OnlineTestManagement.entity.TestAssignment;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.exception.ApiException;
import com.example.OnlineTestManagement.repository.ClassRoomRepository;
import com.example.OnlineTestManagement.repository.QuestionRepository;
import com.example.OnlineTestManagement.repository.TagRepository;
import com.example.OnlineTestManagement.repository.TestAssignmentRepository;
import com.example.OnlineTestManagement.repository.TestRepository;
import com.example.OnlineTestManagement.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.*;

@Service
public class TestService {
    private final TestRepository testRepository;
    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TestAssignmentRepository testAssignmentRepository;

    public TestService(TestRepository testRepository, TagRepository tagRepository,
            QuestionRepository questionRepository, UserRepository userRepository,
            ClassRoomRepository classRoomRepository, TestAssignmentRepository testAssignmentRepository) {
        this.testRepository = testRepository;
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.classRoomRepository = classRoomRepository;
        this.testAssignmentRepository = testAssignmentRepository;
    }

    @Transactional
    public TestDTOs.TestSummary createTest(User teacher, TestDTOs.GenerateTestRequest generateTestRequest) {
        List<Tag> tags = tagRepository.findAllById(generateTestRequest.tagIds());

        if (tags.size() != new HashSet<>(generateTestRequest.tagIds()).size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more tags do not exist");
        }

        List<Question> questions = questionRepository.findDistinctByTeacherAndTagIds(teacher.getId(),
                generateTestRequest.tagIds());
        if (generateTestRequest.numberOfQuestions() > questions.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Requested number exceeds available matching questions: " + questions.size());
        }

        Collections.shuffle(questions);

        Test test = new Test();
        test.setTitle(generateTestRequest.title().trim());
        test.setTeacher(teacher);
        test.getQuestions().addAll(questions.subList(0, generateTestRequest.numberOfQuestions()));

        test = testRepository.save(test);
        return summary(test);

    }

    public void assign(User teacher, Long testId, TestDTOs.AssignTestRequest assignTestRequest) {
        Test test = testRepository.findByIdAndTeacherId(testId, teacher.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Test not found"));

        TestAssignment testAssignment = new TestAssignment();
        testAssignment.setTest(test);

        if (assignTestRequest.studentId() != null) {
            User user = userRepository.findById(assignTestRequest.studentId())
                    .filter(u -> u.getRole() == Role.STUDENT && u.getCreatedByTeacher() != null
                            && teacher.getId().equals(u.getCreatedByTeacher().getId()))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
            testAssignment.setStudent(user);
        } else {
            ClassRoom cl = classRoomRepository.findByIdAndTeacherId(assignTestRequest.classId(), teacher.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));
            testAssignment.setClassRoom(cl);
        }

        boolean exist = testAssignmentRepository.findAllByTestTeacherIdOrderByAssignedAtDesc(teacher.getId()).stream()
                .anyMatch(a -> Objects.equals(a.getTest().getId(), testId)
                        && Objects.equals(a.getClassRoom() == null ? null : a.getClassRoom().getId(),
                                assignTestRequest.classId())
                        && Objects.equals(a.getStudent() == null ? null : a.getStudent().getId(),
                                assignTestRequest.studentId()));
        if (!exist)
            testAssignmentRepository.save(testAssignment);
    }

    private TestDTOs.TestSummary summary(Test test) {
        return new TestDTOs.TestSummary(test.getId(), test.getTitle(), test.getQuestions().size(), test.getCreatedAt());
    }
}
