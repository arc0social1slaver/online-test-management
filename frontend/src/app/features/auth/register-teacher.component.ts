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

@Component({
  standalone: true,
  selector: "app-register-teacher",
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: ` <div class="auth-page">
    <div class="auth-card">
      <div class="brand" style="padding:0 0 18px">
        <div class="brand-mark">OT</div>
        <div><strong>Online Test</strong><small>Management</small></div>
      </div>
      <h1>Create teacher account</h1>
      <p>
        Set up your account to manage students, questions, classes and tests.
      </p>
      <div *ngIf="error" class="alert" style="margin-bottom:14px">
        {{ error }}
      </div>
      <div *ngIf="done" class="alert success" style="margin-bottom:14px">
        Account created. Redirecting…
      </div>
      <form [formGroup]="form" (ngSubmit)="submit()" class="stack">
        <div class="field">
          <label>Email</label
          ><input
            type="email"
            formControlName="email"
            placeholder="teacher@example.com"
          /><small
            class="error"
            *ngIf="
              form.controls['email'].touched && form.controls['email'].invalid
            "
            >Enter a valid email.</small
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
            >Password must be 8–100 characters.</small
          >
        </div>
        <button class="btn btn-primary" type="submit" [disabled]="loading">
          {{ loading ? "Creating…" : "Create account" }}
        </button>
      </form>
      <p style="margin:20px 0 0;text-align:center">
        Already have an account?
        <a routerLink="/login" style="color:var(--primary);font-weight:700"
          >Sign in</a
        >
      </p>
    </div>
  </div>`,
})
export class RegisterTeacherComponent {
  readonly form: FormGroup;
  loading = false;
  error = "";
  done = false;
  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {
    this.form = this.fb.nonNullable.group({
      email: ["", [Validators.required, Validators.email]],
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
  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = "";
    const v = this.form.getRawValue();
    this.auth.teacherRegister(v.email, v.password).subscribe({
      next: (r) => {
        this.done = true;
        this.router.navigateByUrl("/teacher/dashboard");
      },
      error: (e) => {
        this.loading = false;
        this.error = e?.error?.message || "Could not create account.";
      },
      complete: () => (this.loading = false),
    });
  }
}
