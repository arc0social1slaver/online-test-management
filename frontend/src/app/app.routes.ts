import { Routes } from "@angular/router";
import { authGuard } from "./core/guards/auth.guard";
import { roleGuard } from "./core/guards/role.guard";
import { LoginComponent } from "./features/auth/login.component";
import { RegisterTeacherComponent } from "./features/auth/register-teacher.component";
import { ShellComponent } from "./shared/shell.component";
import { TeacherDashboardComponent } from "./features/teacher/dashboard/teacher-dashboard.component";
import { StudentsComponent } from "./features/teacher/students/students.component";
import { ClassesComponent } from "./features/teacher/classes/classes.component";
import { QuestionsComponent } from "./features/teacher/questions/questions.component";
import { TestsComponent } from "./features/teacher/tests/tests.component";
import { TeacherResultsComponent } from "./features/teacher/results/results.component";
import { TeacherProfileComponent } from "./features/teacher/profile/profile.component";
import { StudentDashboardComponent } from "./features/student/dashboard/student-dashboard.component";
import { TakeTestComponent } from "./features/student/take-test/take-test.component";
import { StudentResultsComponent } from "./features/student/results/results.component";
import { StudentProfileComponent } from "./features/student/profile/profile.component";

export const routes: Routes = [
  { path: "", pathMatch: "full", redirectTo: "login" },
  { path: "login", component: LoginComponent },
  { path: "register/teacher", component: RegisterTeacherComponent },
  {
    path: "",
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: "teacher",
        canActivate: [roleGuard("TEACHER")],
        children: [
          { path: "", pathMatch: "full", redirectTo: "dashboard" },
          { path: "dashboard", component: TeacherDashboardComponent },
          { path: "students", component: StudentsComponent },
          { path: "classes", component: ClassesComponent },
          { path: "questions", component: QuestionsComponent },
          { path: "tests", component: TestsComponent },
          { path: "results", component: TeacherResultsComponent },
          { path: "profile", component: TeacherProfileComponent },
        ],
      },
      {
        path: "student",
        canActivate: [roleGuard("STUDENT")],
        children: [
          { path: "", pathMatch: "full", redirectTo: "dashboard" },
          { path: "dashboard", component: StudentDashboardComponent },
          { path: "take/:testId", component: TakeTestComponent },
          { path: "results", component: StudentResultsComponent },
          { path: "profile", component: StudentProfileComponent },
        ],
      },
    ],
  },
  { path: "**", redirectTo: "login" },
];
