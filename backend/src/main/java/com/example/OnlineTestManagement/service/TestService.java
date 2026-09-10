package com.example.OnlineTestManagement.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.OnlineTestManagement.dto.TestDTOs;
import com.example.OnlineTestManagement.entity.ClassRoom;
import com.example.OnlineTestManagement.entity.Question;
import com.example.OnlineTestManagement.entity.Role;
import com.example.OnlineTestManagement.entity.Tag;
import com.example.OnlineTestManagement.entity.Test;
import com.example.OnlineTestManagement.entity.TestAnswer;
import com.example.OnlineTestManagement.entity.TestAssignment;
import com.example.OnlineTestManagement.entity.TestAttempt;
import com.example.OnlineTestManagement.entity.User;
import com.example.OnlineTestManagement.exception.ApiException;
import com.example.OnlineTestManagement.repository.ClassRoomRepository;
import com.example.OnlineTestManagement.repository.QuestionRepository;
import com.example.OnlineTestManagement.repository.TagRepository;
import com.example.OnlineTestManagement.repository.TestAssignmentRepository;
import com.example.OnlineTestManagement.repository.TestAttemptRepository;
import com.example.OnlineTestManagement.repository.TestRepository;
import com.example.OnlineTestManagement.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class TestService {
        private final TestRepository testRepository;
        private final TagRepository tagRepository;
        private final QuestionRepository questionRepository;
        private final UserRepository userRepository;
        private final ClassRoomRepository classRoomRepository;
        private final TestAssignmentRepository testAssignmentRepository;
        private final TestAttemptRepository testAttemptRepository;

        public TestService(TestRepository testRepository, TagRepository tagRepository,
                        QuestionRepository questionRepository, UserRepository userRepository,
                        ClassRoomRepository classRoomRepository, TestAssignmentRepository testAssignmentRepository,
                        TestAttemptRepository testAttemptRepository) {
                this.testRepository = testRepository;
                this.tagRepository = tagRepository;
                this.questionRepository = questionRepository;
                this.userRepository = userRepository;
                this.classRoomRepository = classRoomRepository;
                this.testAssignmentRepository = testAssignmentRepository;
                this.testAttemptRepository = testAttemptRepository;
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

        @Transactional
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
                        ClassRoom cl = classRoomRepository
                                        .findByIdAndTeacherId(assignTestRequest.classId(), teacher.getId())
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found"));
                        testAssignment.setClassRoom(cl);
                }

                boolean exist = testAssignmentRepository.findAllByTestTeacherIdOrderByAssignedAtDesc(teacher.getId())
                                .stream()
                                .anyMatch(a -> Objects.equals(a.getTest().getId(), testId)
                                                && Objects.equals(
                                                                a.getClassRoom() == null ? null
                                                                                : a.getClassRoom().getId(),
                                                                assignTestRequest.classId())
                                                && Objects.equals(
                                                                a.getStudent() == null ? null : a.getStudent().getId(),
                                                                assignTestRequest.studentId()));
                if (!exist)
                        testAssignmentRepository.save(testAssignment);
        }

        public List<TestDTOs.TestSummary> studAvailableTest(User student) {
                Set<Long> accessible = new LinkedHashSet<>(
                                testAssignmentRepository.findAccessibleTestIdsForStudent(student.getId()));
                Set<Long> completed = new HashSet<>();
                testAttemptRepository.findAllByStudentIdOrderBySubmittedAtDesc(student.getId())
                                .forEach(i -> completed.add(i.getTest().getId()));
                accessible.removeAll(completed);
                return testRepository.findAllById(accessible).stream().map(this::summary).toList();
        }

        public List<TestDTOs.TestSummary> studCompletedTest(User student) {
                return testAttemptRepository.findAllByStudentIdOrderBySubmittedAtDesc(student.getId()).stream()
                                .map(i -> summary(i.getTest())).toList();
        }

        @Transactional(readOnly = true)
        public TestDTOs.TakeTestResponse openTest(User student, Long testId) {
                if (testAttemptRepository.findByTestIdAndStudentId(testId, student.getId()).isPresent()) {
                        throw new ApiException(HttpStatus.CONFLICT, "Test already completed");
                }
                if (!testAssignmentRepository.findAccessibleTestIdsForStudent(student.getId()).contains(testId)) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Test is not assigned to this student");
                }
                Test test = testRepository.findById(testId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Test not found"));
                List<TestDTOs.TakeQuestion> questions = test
                                .getQuestions().stream().map(q -> new TestDTOs.TakeQuestion(q.getId(), q.getText(), List
                                                .of(q.getChoiceA(), q.getChoiceB(), q.getChoiceC(), q.getChoiceD())))
                                .toList();
                return new TestDTOs.TakeTestResponse(test.getId(), test.getTitle(), questions);
        }

        @Transactional
        public TestDTOs.ResultDetail submitTest(User student, Long testId,
                        TestDTOs.SubmitTestRequest submitTestRequest) {
                if (testAttemptRepository.findByTestIdAndStudentId(testId, student.getId()).isPresent()) {
                        throw new ApiException(HttpStatus.CONFLICT, "Test already completed");
                }
                if (!testAssignmentRepository.findAccessibleTestIdsForStudent(student.getId()).contains(testId)) {
                        throw new ApiException(HttpStatus.FORBIDDEN, "Test is not assigned to this student");
                }
                Test test = testRepository.findById(testId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Test not found"));

                Map<Long, Integer> submitted = new HashMap<>();
                for (TestDTOs.AnswerSubmission a : submitTestRequest.answers()) {
                        if (submitted.put(a.questionId(), a.answerIndex()) != null)
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Duplicate answer for question " + a.questionId());
                }
                Set<Long> testQuestionIds = new HashSet<>(test.getQuestions().stream().map(Question::getId).toList());
                if (!testQuestionIds.equals(submitted.keySet())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Answers must contain exactly one answer for every test question");
                }

                TestAttempt attempt = new TestAttempt();
                attempt.setTest(test);
                attempt.setStudent(student);
                attempt.setSubmittedAt(Instant.now());

                int score = 0;
                for (Question q : test.getQuestions()) {
                        int ansIdx = submitted.get(q.getId());
                        TestAnswer ans = new TestAnswer();
                        ans.setAttempt(attempt);
                        ans.setQuestionId(q.getId());
                        ans.setQuestionText(q.getText());
                        ans.setChoiceA(q.getChoiceA());
                        ans.setChoiceB(q.getChoiceB());
                        ans.setChoiceC(q.getChoiceC());
                        ans.setChoiceD(q.getChoiceD());
                        ans.setStudentAnswerIndex(ansIdx);
                        ans.setCorrectAnswerIndex(q.getCorrectIndex());
                        attempt.getAnswers().add(ans);
                        if (ansIdx == q.getCorrectIndex())
                                score++;
                }
                attempt.setScore(score);
                attempt = testAttemptRepository.save(attempt);
                return toDetail(attempt);
        }

        public List<TestDTOs.ResultSummary> teacherResults(User teacher) {
                return testAttemptRepository.findAllByTestTeacherIdOrderBySubmittedAtDesc(teacher.getId()).stream()
                                .map(this::toSummary).toList();
        }

        public List<TestDTOs.ResultSummary> studentResults(User student) {
                return testAttemptRepository.findAllByStudentIdOrderBySubmittedAtDesc(student.getId()).stream()
                                .map(this::toSummary).toList();
        }

        public TestDTOs.ResultDetail teacherResult(User teacher, Long resultId) {
                TestAttempt testAttempt = testAttemptRepository.findByIdAndTestTeacherId(resultId, teacher.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Result not found"));

                return toDetail(testAttempt);
        }

        public TestDTOs.ResultDetail studentResult(User student, Long resultId) {
                TestAttempt testAttempt = testAttemptRepository.findByIdAndStudentId(resultId, student.getId())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Result not found"));
                return toDetail(testAttempt);
        }

        private TestDTOs.TestSummary summary(Test test) {
                return new TestDTOs.TestSummary(test.getId(), test.getTitle(), test.getQuestions().size(),
                                test.getCreatedAt());
        }

        private TestDTOs.ResultSummary toSummary(TestAttempt a) {
                return new TestDTOs.ResultSummary(a.getId(), a.getTest().getId(), a.getTest().getTitle(),
                                a.getStudent().getId(), a.getStudent().getUsername(), a.getScore(),
                                a.getAnswers().size(), a.getSubmittedAt());
        }

        private TestDTOs.ResultDetail toDetail(TestAttempt a) {
                List<TestDTOs.ResultAnswer> answers = a.getAnswers().stream()
                                .map(x -> new TestDTOs.ResultAnswer(x.getQuestionId(), x.getQuestionText(),
                                                List.of(x.getChoiceA(), x.getChoiceB(), x.getChoiceC(), x.getChoiceD()),
                                                x.getStudentAnswerIndex(), x.getCorrectAnswerIndex()))
                                .toList();
                return new TestDTOs.ResultDetail(a.getId(), a.getTest().getId(), a.getTest().getTitle(),
                                a.getStudent().getId(), a.getStudent().getUsername(), a.getScore(), answers.size(),
                                a.getSubmittedAt(), answers);
        }
}
