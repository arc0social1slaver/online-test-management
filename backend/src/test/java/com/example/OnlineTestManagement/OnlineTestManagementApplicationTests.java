package com.example.OnlineTestManagement;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.http.MediaType;

import java.util.TimeZone;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OnlineTestManagementApplicationTests {

	@Container
	@SuppressWarnings("resource")
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("online_test")
			.withUsername("postgres")
			.withPassword("postgres");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@BeforeAll
	static void setTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@DynamicPropertySource
	static void configureDatabase(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("app.jwt.secret", () -> "integration-test-secret-that-is-at-least-256-bits-long-123456");
		registry.add("app.jwt.expiration-ms", () -> "3600000");
	}

	@Test
	@Order(1)
	void teacherCannotAccessStudentApi() throws Exception {
		String teacherToken = registerTeacher();

		mockMvc.perform(get("/api/student/tests/available")
				.header("Authorization", "Bearer " + teacherToken))
				.andExpect(status().isForbidden());
	}

	@Test
	@Order(2)
	void completeStudentTestJourneyAndPersistResult() throws Exception {
		String teacherToken = registerTeacher();

		String username = "student_" + UUID.randomUUID().toString().substring(0, 8);
		mockMvc.perform(post("/api/teacher/students")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "%s",
						  "password": "Password123"
						}
						""".formatted(username)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(username)));

		long tagId = objectMapper.readTree(
				mockMvc.perform(get("/api/teacher/tags")
						.header("Authorization", "Bearer " + teacherToken))
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
						.getContentAsString())
				.get(0).get("id").asLong();

		mockMvc.perform(post("/api/teacher/questions")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "text": "2 + 2 = ?",
						  "choiceA": "3",
						  "choiceB": "4",
						  "choiceC": "5",
						  "choiceD": "6",
						  "correctIndex": 1,
						  "tagIds": [%d]
						}
						""".formatted(tagId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correctIndex", is(1)));

		String testResponse = mockMvc.perform(post("/api/teacher/tests")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "title": "Integration Test",
						  "tagIds": [%d],
						  "numberOfQuestions": 1
						}
						""".formatted(tagId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numberOfQuestions", is(1)))
				.andReturn()
				.getResponse()
				.getContentAsString();
		long testId = objectMapper.readTree(testResponse).get("id").asLong();

		String studentListResponse = mockMvc.perform(get("/api/teacher/students")
				.header("Authorization", "Bearer " + teacherToken))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		long studentId = objectMapper.readTree(studentListResponse).get(0).get("id").asLong();

		mockMvc.perform(post("/api/teacher/tests/{testId}/assign", testId)
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"studentId\": %d}".formatted(studentId)))
				.andExpect(status().isOk());

		String studentToken = loginStudent(username, "Password123");

		mockMvc.perform(get("/api/student/tests/available")
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is((int) testId)));

		String openResponse = mockMvc.perform(get("/api/student/tests/{testId}", testId)
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.questions", hasSize(1)))
				.andReturn()
				.getResponse()
				.getContentAsString();
		long questionId = objectMapper.readTree(openResponse).get("questions").get(0).get("questionId").asLong();

		mockMvc.perform(post("/api/student/tests/{testId}/submit", testId)
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"answers\": []}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/student/tests/{testId}/submit", testId)
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"answers\":[{\"questionId\":%d,\"answerIndex\":1}]}".formatted(questionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.testId", is((int) testId)))
				.andExpect(jsonPath("$.score", is(1)))
				.andExpect(jsonPath("$.totalQuestions", is(1)))
				.andExpect(jsonPath("$.answers[0].studentAnswerIndex", is(1)))
				.andExpect(jsonPath("$.answers[0].correctAnswerIndex", is(1)));

		mockMvc.perform(post("/api/student/tests/{testId}/submit", testId)
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"answers\":[{\"questionId\":%d,\"answerIndex\":1}]}".formatted(questionId)))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/api/student/tests/completed")
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is((int) testId)));

		String resultsResponse = mockMvc.perform(get("/api/student/results")
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].testId", is((int) testId)))
				.andExpect(jsonPath("$[0].score", is(1)))
				.andReturn()
				.getResponse()
				.getContentAsString();
		long resultId = objectMapper.readTree(resultsResponse).get(0).get("resultId").asLong();

		mockMvc.perform(get("/api/student/results/{resultId}", resultId)
				.header("Authorization", "Bearer " + studentToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultId", is((int) resultId)))
				.andExpect(jsonPath("$.answers", hasSize(1)));

		mockMvc.perform(get("/api/teacher/results")
				.header("Authorization", "Bearer " + teacherToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].resultId", is((int) resultId)))
				.andExpect(jsonPath("$[0].score", is(1)));
	}

	private String registerTeacher() throws Exception {
		String email = "teacher_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
		String response = mockMvc.perform(post("/api/auth/teacher/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "%s",
						  "password": "Password123"
						}
						""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role", is("TEACHER")))
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(response).get("token").asText();
	}

	private String loginStudent(String username, String password) throws Exception {
		String response = mockMvc.perform(post("/api/auth/student/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "username": "%s",
						  "password": "%s"
						}
						""".formatted(username, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role", is("STUDENT")))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode json = objectMapper.readTree(response);
		return json.get("token").asText();
	}
}
