import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink } from "@angular/router";
import { forkJoin } from "rxjs";
import { TeacherApiService } from "../../../core/services/teacher-api.service";
import { TestSummary, ResultSummary } from "../../../core/models/api.models";
import { formatDate } from "../../../shared/format";

@Component({
  standalone: true,
  selector: "app-teacher-dashboard",
  imports: [CommonModule, RouterLink],
  template: ` <div class="page-title">
      <div>
        <h1>Teacher Dashboard</h1>
        <p>Manage your students and assessment workspace.</p>
      </div>
      <div class="actions">
        <a routerLink="/teacher/tests" class="btn btn-primary"
          >+ Generate test</a
        >
      </div>
    </div>
    <div class="grid grid-4">
      <div class="card stat">
        <div class="label">Students</div>
        <div class="value">{{ students }}</div>
        <div class="hint">Accounts you created</div>
      </div>
      <div class="card stat">
        <div class="label">Classes</div>
        <div class="value">{{ classes }}</div>
        <div class="hint">Your active classes</div>
      </div>
      <div class="card stat">
        <div class="label">Questions</div>
        <div class="value">{{ questions }}</div>
        <div class="hint">Question bank</div>
      </div>
      <div class="card stat">
        <div class="label">Tests</div>
        <div class="value">{{ tests.length }}</div>
        <div class="hint">Generated tests</div>
      </div>
    </div>
    <div class="grid grid-2 mt">
      <div class="card">
        <div class="page-title" style="margin-bottom:8px">
          <div>
            <h2 style="font-size:18px;margin:0">Recent tests</h2>
            <p>Latest generated assessments.</p>
          </div>
          <a routerLink="/teacher/tests" class="btn btn-sm">View all</a>
        </div>
        <div *ngIf="tests.length === 0" class="empty">
          <h3>No tests yet</h3>
          <p>Generate a test from matching tags.</p>
        </div>
        <div class="table-wrap" *ngIf="tests.length">
          <table class="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Questions</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let t of tests | slice: 0 : 5">
                <td>
                  <strong>{{ t.title }}</strong>
                </td>
                <td>{{ t.numberOfQuestions }}</td>
                <td>{{ date(t.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card">
        <div class="page-title" style="margin-bottom:8px">
          <div>
            <h2 style="font-size:18px;margin:0">Latest submissions</h2>
            <p>Recent student results.</p>
          </div>
          <a routerLink="/teacher/results" class="btn btn-sm">View all</a>
        </div>
        <div *ngIf="!results.length" class="empty">
          <h3>No submissions</h3>
          <p>Completed tests will appear here.</p>
        </div>
        <div class="table-wrap" *ngIf="results.length">
          <table class="table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Score</th>
                <th>Submitted</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let r of results | slice: 0 : 5">
                <td>
                  <strong>{{ r.studentUsername }}</strong>
                  <div class="muted">{{ r.testTitle }}</div>
                </td>
                <td>{{ r.score }} / {{ r.totalQuestions }}</td>
                <td>{{ date(r.submittedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>`,
})
export class TeacherDashboardComponent {
  students = 0;
  classes = 0;
  questions = 0;
  tests: TestSummary[] = [];
  results: ResultSummary[] = [];
  constructor(private readonly api: TeacherApiService) {}
  ngOnInit() {
    forkJoin({
      s: this.api.students(),
      c: this.api.classes(),
      q: this.api.questions(),
      t: this.api.tests(),
      r: this.api.results(),
    }).subscribe((x) => {
      this.students = x.s.length;
      this.classes = x.c.length;
      this.questions = x.q.length;
      this.tests = x.t;
      this.results = x.r;
    });
  }
  date(v: string) {
    return formatDate(v);
  }
}
