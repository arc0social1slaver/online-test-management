import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResultDetail, ResultSummary, TakeTest, TestSummary } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class StudentApiService {
  constructor(private readonly http: HttpClient) {}
  availableTests() { return this.http.get<TestSummary[]>('/api/student/tests/available'); }
  completedTests() { return this.http.get<TestSummary[]>('/api/student/tests/completed'); }
  openTest(id: number) { return this.http.get<TakeTest>(`/api/student/tests/${id}`); }
  submit(id: number, answers: { questionId: number; answerIndex: number }[]) { return this.http.post<ResultDetail>(`/api/student/tests/${id}/submit`, { answers }); }
  results() { return this.http.get<ResultSummary[]>('/api/student/results'); }
  result(id: number) { return this.http.get<ResultDetail>(`/api/student/results/${id}`); }
}
