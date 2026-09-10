import { Injectable, computed, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { tap } from "rxjs";
import { AuthResponse, Role, UserResponse } from "../models/api.models";

@Injectable({ providedIn: "root" })
export class AuthService {
  private readonly tokenKey = "otm_token";
  private readonly roleKey = "otm_role";
  private readonly userKey = "otm_user";
  private readonly currentUserSignal = signal<UserResponse | null>(
    this.readUser(),
  );
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!localStorage.getItem(this.tokenKey));

  constructor(private readonly http: HttpClient) {}

  teacherRegister(email: string, password: string) {
    return this.http
      .post<AuthResponse>("/api/auth/teacher/register", { email, password })
      .pipe(tap((r) => this.saveAuth(r)));
  }
  teacherLogin(email: string, password: string) {
    return this.http
      .post<AuthResponse>("/api/auth/teacher/login", { email, password })
      .pipe(tap((r) => this.saveAuth(r)));
  }
  studentLogin(username: string, password: string) {
    return this.http
      .post<AuthResponse>("/api/auth/student/login", { username, password })
      .pipe(tap((r) => this.saveAuth(r)));
  }
  me() {
    return this.http.get<UserResponse>("/api/auth/me").pipe(
      tap((u) => {
        this.currentUserSignal.set(u);
        localStorage.setItem(this.userKey, JSON.stringify(u));
      }),
    );
  }
  changePassword(currentPassword: string, newPassword: string) {
    return this.http.post<void>("/api/auth/change-password", {
      currentPassword,
      newPassword,
    });
  }
  token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }
  role(): Role | null {
    return localStorage.getItem(this.roleKey) as Role | null;
  }
  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSignal.set(null);
  }
  private saveAuth(r: AuthResponse) {
    localStorage.setItem(this.tokenKey, r.token);
    localStorage.setItem(this.roleKey, r.role);
    this.me().subscribe({ error: () => this.currentUserSignal.set(null) });
  }
  private readUser(): UserResponse | null {
    try {
      return JSON.parse(
        localStorage.getItem(this.userKey) ?? "null",
      ) as UserResponse | null;
    } catch {
      return null;
    }
  }
}
