import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { TeacherApiService } from "../../../core/services/teacher-api.service";
import { ResultDetail, ResultSummary } from "../../../core/models/api.models";
import { formatDate } from "../../../shared/format";

@Component({
  standalone: true,
  selector: "app-teacher-results",
  imports: [CommonModule],
  template: ` <div class="page-title">
      <div>
        <h1>Results</h1>
        <p>Review submissions for tests you created.</p>
      </div>
    </div>
    <div class="grid grid-2" *ngIf="selected">
      <div class="card">
        <div class="page-title" style="margin-bottom:16px">
          <div>
            <h2 style="margin:0;font-size:18px">{{ selected.testTitle }}</h2>
            <p>
              {{ selected.studentUsername }} · {{ date(selected.submittedAt) }}
            </p>
          </div>
          <button class="btn btn-sm" (click)="selected = null">Close</button>
        </div>
        <div class="inline" style="align-items:flex-end;gap:16px">
          <div>
            <div class="result-score">{{ selected.score }}</div>
            <div class="muted">out of {{ selected.totalQuestions }}</div>
          </div>
          <span
            class="badge"
            [class.badge-success]="selected.score === selected.totalQuestions"
            >{{
              (selected.score / selected.totalQuestions) * 100
                | number: "1.0-0"
            }}%</span
          >
        </div>
        <div class="mt">
          <div
            class="result-answer"
            *ngFor="let a of selected.answers; let i = index"
          >
            <div class="muted">Question {{ i + 1 }}</div>
            <h3 style="font-size:15px">{{ a.questionText }}</h3>
            <div class="choice-grid">
              <div
                *ngFor="let c of a.choices; let j = index"
                class="choice"
                [class.selected]="j === a.studentAnswerIndex"
              >
                <span
                  class="answer-chip"
                  [class.correct]="j === a.correctAnswerIndex"
                  [class.wrong]="
                    j === a.studentAnswerIndex && j !== a.correctAnswerIndex
                  "
                  >{{ letters[j] }}</span
                ><span style="flex:1">{{ c }}</span
                ><span class="muted" *ngIf="j === a.correctAnswerIndex"
                  >Correct</span
                ><span class="muted" *ngIf="j === a.studentAnswerIndex"
                  >Student answer</span
                >
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="card" [class.grid-2]="selected">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Student</th>
              <th>Test</th>
              <th>Score</th>
              <th>Submitted</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of results">
              <td>{{ r.studentUsername }}</td>
              <td>{{ r.testTitle }}</td>
              <td>
                <strong>{{ r.score }} / {{ r.totalQuestions }}</strong>
              </td>
              <td>{{ date(r.submittedAt) }}</td>
              <td>
                <button class="btn btn-sm" (click)="open(r.resultId)">
                  Open
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div *ngIf="!results.length" class="empty">
        <h3>No results</h3>
        <p>Student submissions will appear here.</p>
      </div>
    </div>`,
})
export class TeacherResultsComponent {
  results: ResultSummary[] = [];
  selected: ResultDetail | null = null;
  letters = ["A", "B", "C", "D"];
  constructor(private readonly api: TeacherApiService) {}
  ngOnInit() {
    this.api.results().subscribe((r) => (this.results = r));
  }
  open(id: number) {
    this.api.result(id).subscribe((r) => (this.selected = r));
  }
  date(v: string) {
    return formatDate(v);
  }
}
