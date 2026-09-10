import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink } from "@angular/router";
import { forkJoin } from "rxjs";
import { StudentApiService } from "../../../core/services/student-api.service";
import { TestSummary, ResultSummary } from "../../../core/models/api.models";
import { formatDate } from "../../../shared/format";

@Component({
  standalone: true,
  selector: "app-student-dashboard",
  imports: [CommonModule, RouterLink],
  template: ` <div class="page-title">
      <div>
        <h1>Student Dashboard</h1>
        <p>Take assigned tests and review your completed results.</p>
      </div>
    </div>
    <div class="hero">
      <div class="card">
        <h2 class="hero-title">Keep learning, one test at a time.</h2>
        <p>Your assigned tests appear here. Each test can be submitted once.</p>
        <div class="kpi-row">
          <div class="kpi">
            <strong>{{ available.length }}</strong
            ><span>Available</span>
          </div>
          <div class="kpi">
            <strong>{{ completed.length }}</strong
            ><span>Completed</span>
          </div>
          <div class="kpi">
            <strong>{{ results.length }}</strong
            ><span>Results</span>
          </div>
        </div>
      </div>
      <div class="card">
        <h2 style="font-size:18px;margin:0 0 12px">Latest result</h2>
        <div *ngIf="results.length; else noResult">
          <div class="result-score">
            {{ results[0].score }}/{{ results[0].totalQuestions }}
          </div>
          <strong>{{ results[0].testTitle }}</strong>
          <div class="muted">{{ date(results[0].submittedAt) }}</div>
          <a
            routerLink="/student/results"
            class="btn btn-sm"
            style="margin-top:12px;display:inline-flex"
            >View result</a
          >
        </div>
        <ng-template #noResult
          ><div class="empty">
            <h3>No results yet</h3>
            <p>Complete a test to see your score.</p>
          </div></ng-template
        >
      </div>
    </div>
    <div class="grid grid-2 mt">
      <div class="card">
        <div class="page-title" style="margin-bottom:8px">
          <div>
            <h2 style="font-size:18px;margin:0">Available tests</h2>
            <p>Tests you can take now.</p>
          </div>
        </div>
        <div *ngIf="available.length" class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Test</th>
                <th>Questions</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let t of available">
                <td>
                  <strong>{{ t.title }}</strong>
                  <div class="muted">{{ date(t.createdAt) }}</div>
                </td>
                <td>{{ t.numberOfQuestions }}</td>
                <td>
                  <a
                    class="btn btn-sm btn-primary"
                    [routerLink]="['/student/take', t.id]"
                    >Start</a
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div *ngIf="!available.length" class="empty">
          <h3>All caught up</h3>
          <p>No assigned tests are currently available.</p>
        </div>
      </div>
      <div class="card">
        <h2 style="font-size:18px;margin:0 0 8px">Completed tests</h2>
        <p class="muted">Your submitted assessments.</p>
        <div class="table-wrap" *ngIf="completed.length">
          <table class="table">
            <thead>
              <tr>
                <th>Test</th>
                <th>Questions</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let t of completed">
                <td>{{ t.title }}</td>
                <td>{{ t.numberOfQuestions }}</td>
                <td>
                  <a routerLink="/student/results" class="btn btn-sm"
                    >Results</a
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div *ngIf="!completed.length" class="empty">
          <h3>No completed tests</h3>
          <p>Your completed tests will stay in your history.</p>
        </div>
      </div>
    </div>`,
})
export class StudentDashboardComponent {
  available: TestSummary[] = [];
  completed: TestSummary[] = [];
  results: ResultSummary[] = [];
  constructor(private readonly api: StudentApiService) {}
  ngOnInit() {
    forkJoin({
      a: this.api.availableTests(),
      c: this.api.completedTests(),
      r: this.api.results(),
    }).subscribe((x) => {
      this.available = x.a;
      this.completed = x.c;
      this.results = x.r;
    });
  }
  date(v: string) {
    return formatDate(v);
  }
}
