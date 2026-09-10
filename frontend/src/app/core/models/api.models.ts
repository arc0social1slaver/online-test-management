export type Role = "TEACHER" | "STUDENT";

export interface AuthResponse {
  userId: number;
  role: Role;
  token: string;
}
export interface UserResponse {
  id: number;
  email: string | null;
  username: string | null;
  role: Role;
}
export interface Tag {
  id: number;
  name: string;
}
export interface Question {
  id: number;
  text: string;
  choices: string[];
  correctIndex: number;
  tags: Tag[];
}
export interface Classroom {
  id: number;
  name: string;
  students: UserResponse[];
}
export interface TestSummary {
  id: number;
  title: string;
  numberOfQuestions: number;
  createdAt: string;
}
export interface TakeQuestion {
  questionId: number;
  text: string;
  choices: string[];
}
export interface TakeTest {
  id: number;
  title: string;
  questions: TakeQuestion[];
}
export interface ResultSummary {
  resultId: number;
  testId: number;
  testTitle: string;
  studentId: number;
  studentUsername: string;
  score: number;
  totalQuestions: number;
  submittedAt: string;
}
export interface ResultAnswer {
  questionId: number;
  questionText: string;
  choices: string[];
  studentAnswerIndex: number;
  correctAnswerIndex: number;
}
export interface ResultDetail extends ResultSummary {
  answers: ResultAnswer[];
}
export interface ApiError {
  message?: string;
  fields?: Record<string, string>;
}
