import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { AuthService } from "../../../core/services/auth.service";

@Component({
  standalone: true,
  selector: "app-teacher-profile",
  imports: [CommonModule, ReactiveFormsModule],
  template: ` <div class="page-title">
      <div>
        <h1>Profile</h1>
        <p>Update your password from your profile.</p>
      </div>
    </div>
    <div class="grid grid-2">
      <div class="card">
        <h2 style="font-size:18px;margin:0 0 14px">Account</h2>
        <div class="stack">
          <div>
            <div class="muted">Email</div>
            <strong>{{ auth.currentUser()?.email }}</strong>
          </div>
          <div>
            <div class="muted">Role</div>
            <span class="badge">Teacher</span>
          </div>
        </div>
      </div>
      <div class="card">
        <h2 style="font-size:18px;margin:0 0 14px">Change password</h2>
        <div
          *ngIf="message"
          class="alert"
          [class.success]="success"
          style="margin-bottom:12px"
        >
          {{ message }}
        </div>
        <form [formGroup]="form" (ngSubmit)="submit()" class="stack">
          <div class="field">
            <label>Current password</label
            ><input type="password" formControlName="currentPassword" />
          </div>
          <div class="field">
            <label>New password</label
            ><input type="password" formControlName="newPassword" /><small
              class="error"
              *ngIf="
                form.controls['newPassword'].touched &&
                form.controls['newPassword'].invalid
              "
              >At least 8 characters.</small
            >
          </div>
          <button class="btn btn-primary">Change password</button>
        </form>
      </div>
    </div>`,
})
export class TeacherProfileComponent {
  message = "";
  success = false;
  readonly form: FormGroup;
  constructor(
    private readonly fb: FormBuilder,
    public readonly auth: AuthService,
  ) {
    this.form = this.fb.nonNullable.group({
      currentPassword: ["", Validators.required],
      newPassword: [
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
    const v = this.form.getRawValue();
    this.auth.changePassword(v.currentPassword, v.newPassword).subscribe({
      next: () => {
        this.success = true;
        this.message = "Password changed successfully.";
        this.form.reset();
      },
      error: (e) => {
        this.success = false;
        this.message = e?.error?.message || "Could not change password.";
      },
    });
  }
}
