import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router } from "@angular/router";
import { StudentApiService } from "../../../core/services/student-api.service";
import { TakeTest } from "../../../core/models/api.models";

@Component({
  standalone: true,
  selector: "app-take-test",
  imports: [CommonModule],
  template: ` <div *ngIf="test; else loadingTpl">
      <div class="page-title">
        <div>
          <h1>{{ test.title }}</h1>
          <p>
            {{ test.questions.length }} questions · Select one answer for every
            question.
          </p>
        </div>
        <button
          class="btn btn-primary"
          (click)="submit()"
          [disabled]="submitting || unanswered > 0"
        >
          {{ submitting ? "Submitting…" : "Submit test" }}
        </button>
      </div>
      <div *ngIf="error" class="alert" style="margin-bottom:16px">
        {{ error }}
      </div>
      <div class="test-layout">
        <div class="card question-nav">
          <h3 style="margin:0 0 12px;font-size:15px">Questions</h3>
          <div class="q-grid">
            <button
              class="q-nav"
              *ngFor="let q of test.questions; let i = index"
              [class.current]="i === current"
              [class.answered]="answers[q.questionId] !== undefined"
              (click)="current = i"
            >
              {{ i + 1 }}
            </button>
          </div>
          <div class="muted" style="margin-top:15px;font-size:12px">
            {{ test.questions.length - unanswered }} /
            {{ test.questions.length }} answered
          </div>
        </div>
        <div class="card">
          <div class="muted">
            Question {{ current + 1 }} of {{ test.questions.length }}
          </div>
          <h2 style="font-size:20px;line-height:1.45">
            {{ test.questions[current].text }}
          </h2>
          <div class="choice-grid">
            <label
              *ngFor="let c of test.questions[current].choices; let j = index"
              class="choice"
              [class.selected]="
                answers[test.questions[current].questionId] === j
              "
              ><input
                type="radio"
                [name]="'q' + test.questions[current].questionId"
                [checked]="answers[test.questions[current].questionId] === j"
                (change)="answer(j)"
              /><span
                ><strong>{{ letters[j] }}.</strong> {{ c }}</span
              ></label
            >
          </div>
          <div
            class="actions"
            style="justify-content:space-between;margin-top:24px"
          >
            <button
              class="btn"
              [disabled]="current === 0"
              (click)="current = current - 1"
            >
              Previous</button
            ><button
              class="btn"
              [disabled]="current === test.questions.length - 1"
              (click)="current = current + 1"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
    <ng-template #loadingTpl
      ><div class="card">
        <div class="empty"><h3>Opening test…</h3></div>
      </div></ng-template
    >`,
})
export class TakeTestComponent {
  test: TakeTest | null = null;
  current = 0;
  answers: Record<number, number> = {};
  letters = ["A", "B", "C", "D"];
  submitting = false;
  error = "";
  private testId = 0;
  constructor(
    private readonly route: ActivatedRoute,
    private readonly api: StudentApiService,
    private readonly router: Router,
  ) {}
  get unanswered() {
    return this.test
      ? this.test.questions.filter(
          (q) => this.answers[q.questionId] === undefined,
        ).length
      : 0;
  }
  ngOnInit() {
    this.testId = Number(this.route.snapshot.paramMap.get("testId"));
    this.api.openTest(this.testId).subscribe({
      next: (t) => (this.test = t),
      error: (e) => {
        this.error = e?.error?.message || "Unable to open this test.";
      },
    });
  }
  answer(index: number) {
    if (this.test)
      this.answers[this.test.questions[this.current].questionId] = index;
  }
  submit() {
    if (!this.test || this.unanswered > 0) return;
    if (!confirm("Submit this test? You can only submit it once.")) return;
    this.submitting = true;
    const payload = this.test.questions.map((q) => ({
      questionId: q.questionId,
      answerIndex: this.answers[q.questionId],
    }));
    this.api.submit(this.test.id, payload).subscribe({
      next: (r) =>
        this.router.navigate(["/student/results"], {
          queryParams: { open: r.resultId },
        }),
      error: (e) => {
        this.submitting = false;
        this.error = e?.error?.message || "Could not submit the test.";
      },
    });
  }
}
