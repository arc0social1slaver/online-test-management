import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { TeacherApiService } from "../../../core/services/teacher-api.service";
import {
  Classroom,
  Tag,
  TestSummary,
  UserResponse,
} from "../../../core/models/api.models";
import { formatDate } from "../../../shared/format";

@Component({
  standalone: true,
  selector: "app-tests",
  imports: [CommonModule, ReactiveFormsModule],
  template: ` <div class="page-title">
      <div>
        <h1>Tests</h1>
        <p>
          Generate a fixed random test from one or more question tags, then
          assign it.
        </p>
      </div>
    </div>
    <div class="grid grid-2">
      <div class="card">
        <h2 style="margin:0 0 16px;font-size:18px">Generate test</h2>
        <div *ngIf="error" class="alert" style="margin-bottom:12px">
          {{ error }}
        </div>
        <form [formGroup]="form" (ngSubmit)="generate()" class="stack">
          <div class="field">
            <label>Title</label
            ><input formControlName="title" placeholder="Midterm Grade 6" />
          </div>
          <div class="field">
            <label>Question tags</label>
            <div class="checkbox-grid">
              <label class="checkbox-pill" *ngFor="let t of tags"
                ><input
                  type="checkbox"
                  [checked]="selectedTags.includes(t.id)"
                  (change)="toggleTag(t.id, $any($event.target).checked)"
                />{{ t.name }}</label
              >
            </div>
            <small class="muted"
              >A question matches when it has at least one selected tag.</small
            >
          </div>
          <div class="field">
            <label>Number of questions</label
            ><input type="number" min="1" formControlName="numberOfQuestions" />
          </div>
          <button class="btn btn-primary" [disabled]="loading">
            {{ loading ? "Generating…" : "Generate test" }}
          </button>
        </form>
      </div>
      <div class="card">
        <h2 style="margin:0 0 16px;font-size:18px">Assign test</h2>
        <p class="muted" *ngIf="!tests.length">
          Generate a test first, then choose a class or individual student.
        </p>
        <form
          *ngIf="tests.length"
          [formGroup]="assignForm"
          (ngSubmit)="assign()"
          class="stack"
        >
          <div class="field">
            <label>Test</label
            ><select formControlName="testId">
              <option [ngValue]="null">Select test…</option>
              <option *ngFor="let t of tests" [ngValue]="t.id">
                {{ t.title }}
              </option>
            </select>
          </div>
          <div class="field">
            <label>Target type</label
            ><select formControlName="targetType">
              <option value="class">Class</option>
              <option value="student">Student</option>
            </select>
          </div>
          <div
            class="field"
            *ngIf="assignForm.controls['targetType'].value === 'class'"
          >
            <label>Class</label
            ><select formControlName="targetId">
              <option [ngValue]="null">Select class…</option>
              <option *ngFor="let c of classes" [ngValue]="c.id">
                {{ c.name }}
              </option>
            </select>
          </div>
          <div
            class="field"
            *ngIf="assignForm.controls['targetType'].value === 'student'"
          >
            <label>Student</label
            ><select formControlName="targetId">
              <option [ngValue]="null">Select student…</option>
              <option *ngFor="let s of students" [ngValue]="s.id">
                {{ s.username }}
              </option>
            </select>
          </div>
          <button class="btn btn-primary" [disabled]="assigning">
            {{ assigning ? "Assigning…" : "Assign test" }}
          </button>
          <div *ngIf="assignSuccess" class="alert success">
            Test assigned successfully.
          </div>
        </form>
      </div>
    </div>
    <div class="card mt">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Questions</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let t of tests">
              <td>
                <strong>{{ t.title }}</strong>
              </td>
              <td>{{ t.numberOfQuestions }}</td>
              <td>{{ date(t.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div *ngIf="!tests.length" class="empty">
        <h3>No tests generated</h3>
        <p>Use the form above to create a test.</p>
      </div>
    </div>`,
})
export class TestsComponent {
  tests: TestSummary[] = [];
  tags: Tag[] = [];
  classes: Classroom[] = [];
  students: UserResponse[] = [];
  selectedTags: number[] = [];
  error = "";
  loading = false;
  assigning = false;
  assignSuccess = false;
  readonly form: FormGroup;
  readonly assignForm: FormGroup;
  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TeacherApiService,
  ) {
    this.form = this.fb.nonNullable.group({
      title: ["", [Validators.required, Validators.maxLength(200)]],
      numberOfQuestions: [1, [Validators.required, Validators.min(1)]],
    });
    this.assignForm = this.fb.group({
      testId: [null as number | null, Validators.required],
      targetType: this.fb.nonNullable.control<"class" | "student">("class"),
      targetId: [null as number | null, Validators.required],
    });
  }
  ngOnInit() {
    this.api.tests().subscribe((t) => (this.tests = t));
    this.api.tags().subscribe((t) => (this.tags = t));
    this.api.classes().subscribe((c) => (this.classes = c));
    this.api.students().subscribe((s) => (this.students = s));
  }
  toggleTag(id: number, c: boolean) {
    const s = new Set(this.selectedTags);
    c ? s.add(id) : s.delete(id);
    this.selectedTags = [...s];
  }
  generate() {
    if (this.form.invalid || !this.selectedTags.length) {
      this.error = "Enter a title and select at least one tag.";
      return;
    }
    this.loading = true;
    this.error = "";
    this.api
      .generateTest({ ...this.form.getRawValue(), tagIds: this.selectedTags })
      .subscribe({
        next: (t) => {
          this.tests = [t, ...this.tests];
          this.form.reset({ title: "", numberOfQuestions: 1 });
          this.selectedTags = [];
        },
        error: (e) =>
          (this.error = e?.error?.message || "Could not generate test."),
        complete: () => (this.loading = false),
      });
  }
  assign() {
    if (this.assignForm.invalid) return;
    const v = this.assignForm.getRawValue();
    this.assigning = true;
    this.assignSuccess = false;
    this.api
      .assignTest(
        v.testId!,
        v.targetType === "class"
          ? { classId: v.targetId! }
          : { studentId: v.targetId! },
      )
      .subscribe({
        next: () => {
          this.assignSuccess = true;
          this.assignForm.controls["targetId"].setValue(null);
        },
        error: (e) =>
          (this.error = e?.error?.message || "Could not assign test."),
        complete: () => (this.assigning = false),
      });
  }
  date(v: string) {
    return formatDate(v);
  }
}
