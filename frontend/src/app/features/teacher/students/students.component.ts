import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { TeacherApiService } from "../../../core/services/teacher-api.service";
import { UserResponse } from "../../../core/models/api.models";

@Component({
  standalone: true,
  selector: "app-students",
  imports: [CommonModule, ReactiveFormsModule],
  template: ` <div class="page-title">
      <div>
        <h1>Students</h1>
        <p>Create and manage student accounts owned by you.</p>
      </div>
      <button class="btn btn-primary" (click)="showForm = !showForm">
        {{ showForm ? "Close" : " + Create student" }}
      </button>
    </div>
    <div *ngIf="showForm" class="card" style="margin-bottom:16px">
      <h2 style="margin:0 0 16px;font-size:18px">New student</h2>
      <div *ngIf="error" class="alert" style="margin-bottom:12px">
        {{ error }}
      </div>
      <form [formGroup]="form" (ngSubmit)="create()" class="form-grid">
        <div class="field">
          <label>Username</label><input formControlName="username" /><small
            class="error"
            *ngIf="
              form.controls['username'].touched &&
              form.controls['username'].invalid
            "
            >Username must be 3–100 characters.</small
          >
        </div>
        <div class="field">
          <label>Temporary password</label
          ><input type="password" formControlName="password" /><small
            class="error"
            *ngIf="
              form.controls['password'].touched &&
              form.controls['password'].invalid
            "
            >Password must be at least 8 characters.</small
          >
        </div>
        <div class="field full actions">
          <button class="btn btn-primary">Create student</button>
        </div>
      </form>
    </div>
    <div class="card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Username</th>
              <th>ID</th>
              <th>Role</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let s of students">
              <td>
                <strong>{{ s.username }}</strong>
              </td>
              <td>{{ s.id }}</td>
              <td><span class="badge">Student</span></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div *ngIf="!students.length" class="empty">
        <h3>No students</h3>
        <p>Create the first student account to start assigning tests.</p>
      </div>
    </div>`,
})
export class StudentsComponent {
  students: UserResponse[] = [];
  showForm = false;
  error = "";
  readonly form: FormGroup;
  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TeacherApiService,
  ) {
    this.form = this.fb.nonNullable.group({
      username: [
        "",
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(100),
        ],
      ],
      password: [
        "",
        [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(100),
        ],
      ],
    });
  }
  ngOnInit() {
    this.load();
  }
  load() {
    this.api.students().subscribe((s) => (this.students = s));
  }
  create() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error = "";
    this.api.createStudent(this.form.getRawValue()).subscribe({
      next: (s) => {
        this.students = [s, ...this.students];
        this.form.reset();
        this.showForm = false;
      },
      error: (e) =>
        (this.error = e?.error?.message || "Could not create student."),
    });
  }
}
