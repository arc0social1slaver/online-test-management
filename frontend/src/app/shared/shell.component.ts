import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from "@angular/router";
import { AuthService } from "../core/services/auth.service";

@Component({
  standalone: true,
  selector: "app-shell",
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-mark">OT</div>
          <div><strong>Online Test</strong><small>Management</small></div>
        </div>
        <div class="user-mini">
          <div class="avatar">{{ initials }}</div>
          <div>
            <strong>{{ displayName }}</strong
            ><small>{{ roleLabel }}</small>
          </div>
        </div>
        <nav>
          <ng-container *ngIf="isTeacher; else studentNav">
            <a routerLink="/teacher/dashboard" routerLinkActive="active"
              ><span>⌂</span> Dashboard</a
            >
            <a routerLink="/teacher/students" routerLinkActive="active"
              ><span>♙</span> Students</a
            >
            <a routerLink="/teacher/classes" routerLinkActive="active"
              ><span>▦</span> Classes</a
            >
            <a routerLink="/teacher/questions" routerLinkActive="active"
              ><span>?</span> Questions</a
            >
            <a routerLink="/teacher/tests" routerLinkActive="active"
              ><span>✓</span> Tests</a
            >
            <a routerLink="/teacher/results" routerLinkActive="active"
              ><span>◔</span> Results</a
            >
          </ng-container>
          <ng-template #studentNav>
            <a routerLink="/student/dashboard" routerLinkActive="active"
              ><span>⌂</span> Dashboard</a
            >
            <a routerLink="/student/results" routerLinkActive="active"
              ><span>◔</span> Results</a
            >
          </ng-template>
        </nav>
        <div class="sidebar-bottom">
          <a
            [routerLink]="isTeacher ? '/teacher/profile' : '/student/profile'"
            routerLinkActive="active"
            ><span>⚙</span> Profile</a
          >
          <button class="link-button" (click)="logout()">
            <span>↪</span> Logout
          </button>
        </div>
      </aside>
      <main class="main">
        <header class="topbar">
          <div></div>
          <div class="top-actions">
            <span class="badge">{{ roleLabel }}</span>
          </div>
        </header>
        <section class="content"><router-outlet /></section>
      </main>
    </div>
  `,
})
export class ShellComponent {
  constructor(
    public readonly auth: AuthService,
    private readonly router: Router,
  ) {}
  get isTeacher() {
    return this.auth.role() === "TEACHER";
  }
  get roleLabel() {
    return this.isTeacher ? "Teacher" : "Student";
  }
  get displayName() {
    const u = this.auth.currentUser();
    return u?.username || u?.email || "User";
  }
  get initials() {
    return this.displayName.slice(0, 2).toUpperCase();
  }
  logout() {
    this.auth.logout();
    this.router.navigateByUrl("/login");
  }
}
