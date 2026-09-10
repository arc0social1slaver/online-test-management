import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { AuthService } from "../../core/services/auth.service";
import { ApiError } from "../../core/models/api.models";

@Component({
  standalone: true,
  selector: "app-login",
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: ` <div class="auth-page">
    <div class="auth-card">
      <div class="brand" style="padding:0 0 18px">
        <div class="brand-mark">OT</div>
        <div><strong>Online Test</strong><small>Management</small></div>
      </div>
      <h1>Welcome back</h1>
      <p>Sign in to continue to your workspace.</p>
      <div class="auth-tabs">
        <button
          [class.active]="mode === 'teacher'"
          (click)="setMode('teacher')"
        >
          Teacher</button
        ><button
          [class.active]="mode === 'student'"
          (click)="setMode('student')"
        >
          Student
        </button>
      </div>
      <div *ngIf="error" class="alert" style="margin-bottom:14px">
        {{ error }}
      </div>
      <form [formGroup]="form" (ngSubmit)="submit()" class="stack">
        <div class="field">
          <label>{{ mode === "teacher" ? "Email" : "Username" }}</label
          ><input
            [type]="mode === 'teacher' ? 'email' : 'text'"
            formControlName="identifier"
            [placeholder]="
              mode === 'teacher' ? 'teacher@example.com' : 'student01'
            "
          /><small
            class="error"
            *ngIf="
              form.controls['identifier'].touched &&
              form.controls['identifier'].invalid
            "
            >{{
              mode === "teacher"
                ? "Enter a valid email."
                : "Username is required."
            }}</small
          >
        </div>
        <div class="field">
          <label>Password</label
          ><input
            type="password"
            formControlName="password"
            placeholder="At least 8 characters"
          /><small
            class="error"
            *ngIf="
              form.controls['password'].touched &&
              form.controls['password'].invalid
            "
            >Password is required.</small
          >
        </div>
        <button class="btn btn-primary" type="submit" [disabled]="loading">
          {{ loading ? "Signing in…" : "Sign in" }}
        </button>
      </form>
      <p *ngIf="mode === 'teacher'" style="margin:20px 0 0;text-align:center">
        New teacher?
        <a
          routerLink="/register/teacher"
          style="color:var(--primary);font-weight:700"
          >Create account</a
        >
      </p>
      <p *ngIf="mode === 'student'" style="margin:20px 0 0;text-align:center">
        Student accounts are created by teachers.
      </p>
    </div>
  </div>`,
})
export class LoginComponent {
  mode: "teacher" | "student" = "teacher";
  loading = false;
  error = "";
  readonly form: FormGroup;
  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {
    this.form = this.fb.nonNullable.group({
      identifier: ["", [Validators.required]],
      password: ["", [Validators.required]],
    });
  }
  setMode(mode: "teacher" | "student") {
    this.mode = mode;
    this.error = "";
    this.form.reset();
    this.form.controls["identifier"].clearValidators();
    this.form.controls["identifier"].addValidators(
      mode === "teacher" ? Validators.email : [],
    );
    this.form.controls["identifier"].updateValueAndValidity();
  }
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = "";
    const v = this.form.getRawValue();
    const req =
      this.mode === "teacher"
        ? this.auth.teacherLogin(v.identifier, v.password)
        : this.auth.studentLogin(v.identifier, v.password);
    req.subscribe({
      next: (r) =>
        this.router.navigateByUrl(
          r.role === "TEACHER" ? "/teacher/dashboard" : "/student/dashboard",
        ),
      error: (e) => {
        this.loading = false;
        this.error = this.message(e);
      },
      complete: () => (this.loading = false),
    });
  }
  private message(e: { error?: ApiError }) {
    return (
      e?.error?.message || "Unable to sign in. Please check your credentials."
    );
  }
}
