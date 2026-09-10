import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  Classroom,
  Question,
  ResultDetail,
  ResultSummary,
  Tag,
  TestSummary,
  UserResponse,
} from "../models/api.models";

@Injectable({ providedIn: "root" })
export class TeacherApiService {
  constructor(private readonly http: HttpClient) {}
  students() {
    return this.http.get<UserResponse[]>("/api/teacher/students");
  }
  createStudent(body: { username: string; password: string }) {
    return this.http.post<UserResponse>("/api/teacher/students", body);
  }
  tags() {
    return this.http.get<Tag[]>("/api/teacher/tags");
  }
  createTag(name: string) {
    return this.http.post<Tag>("/api/teacher/tags", { name });
  }
  questions() {
    return this.http.get<Question[]>("/api/teacher/questions");
  }
  createQuestion(body: {
    text: string;
    choiceA: string;
    choiceB: string;
    choiceC: string;
    choiceD: string;
    correctIndex: number;
    tagIds: number[];
  }) {
    return this.http.post<Question>("/api/teacher/questions", body);
  }
  updateQuestion(
    id: number,
    body: {
      text: string;
      choiceA: string;
      choiceB: string;
      choiceC: string;
      choiceD: string;
      correctIndex: number;
      tagIds: number[];
    },
  ) {
    return this.http.put<Question>(`/api/teacher/questions/${id}`, body);
  }
  deleteQuestion(id: number) {
    return this.http.delete<void>(`/api/teacher/questions/${id}`);
  }
  classes() {
    return this.http.get<Classroom[]>("/api/teacher/classes");
  }
  classDetail(id: number) {
    return this.http.get<Classroom>(`/api/teacher/classes/${id}`);
  }
  createClass(name: string) {
    return this.http.post<Classroom>("/api/teacher/classes", { name });
  }
  updateClass(id: number, name: string) {
    return this.http.put<Classroom>(`/api/teacher/classes/${id}`, { name });
  }
  deleteClass(id: number) {
    return this.http.delete<void>(`/api/teacher/classes/${id}`);
  }
  addStudent(classId: number, studentId: number) {
    return this.http.post<Classroom>(
      `/api/teacher/classes/${classId}/students/${studentId}`,
      {},
    );
  }
  removeStudent(classId: number, studentId: number) {
    return this.http.delete<Classroom>(
      `/api/teacher/classes/${classId}/students/${studentId}`,
    );
  }
  tests() {
    return this.http.get<TestSummary[]>("/api/teacher/tests");
  }
  generateTest(body: {
    title: string;
    tagIds: number[];
    numberOfQuestions: number;
  }) {
    return this.http.post<TestSummary>("/api/teacher/tests", body);
  }
  assignTest(testId: number, body: { classId?: number; studentId?: number }) {
    return this.http.post<void>(`/api/teacher/tests/${testId}/assign`, body);
  }
  results() {
    return this.http.get<ResultSummary[]>("/api/teacher/results");
  }
  result(id: number) {
    return this.http.get<ResultDetail>(`/api/teacher/results/${id}`);
  }
}
