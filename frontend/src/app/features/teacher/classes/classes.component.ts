import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { TeacherApiService } from "../../../core/services/teacher-api.service";
import { Classroom, UserResponse } from "../../../core/models/api.models";

@Component({
  standalone: true,
  selector: "app-classes",
  imports: [CommonModule, ReactiveFormsModule],
  template: ` <div class="page-title">
      <div>
        <h1>Classes</h1>
        <p>Create classes and manage their student membership.</p>
      </div>
      <button
        class="btn btn-primary"
        (click)="editing = null; showForm = !showForm"
      >
        {{ showForm ? "Close" : "+ Create class" }}
      </button>
    </div>
    <div *ngIf="showForm" class="card" style="margin-bottom:16px">
      <h2 style="margin:0 0 14px;font-size:18px">
        {{ editing ? "Edit class" : "New class" }}
      </h2>
      <form [formGroup]="form" (ngSubmit)="save()" class="inline">
        <input
          style="flex:1;max-width:450px"
          formControlName="name"
          placeholder="e.g. Grade 6A"
        /><button class="btn btn-primary">
          {{ editing ? "Save" : "Create" }}
        </button>
      </form>
    </div>
    <div class="grid grid-2">
      <div class="card" *ngFor="let c of classes">
        <div class="page-title" style="margin-bottom:12px">
          <div>
            <h2 style="font-size:18px;margin:0">{{ c.name }}</h2>
            <p>{{ c.students.length }} student(s)</p>
          </div>
          <div class="actions">
            <button class="btn btn-sm" (click)="edit(c)">Edit</button
            ><button class="btn btn-sm btn-danger" (click)="removeClass(c)">
              Delete
            </button>
          </div>
        </div>
        <div class="field" style="margin-bottom:12px">
          <label>Add student</label>
          <div class="inline">
            <select
              [value]="selected[c.id] || ''"
              (change)="selected[c.id] = +$any($event.target).value"
            >
              <option value="">Select student…</option>
              <option *ngFor="let s of available(c)" [value]="s.id">
                {{ s.username }}
              </option></select
            ><button
              class="btn btn-sm"
              [disabled]="!selected[c.id]"
              (click)="add(c)"
            >
              Add
            </button>
          </div>
        </div>
        <div class="tag-list">
          <span class="tag" *ngFor="let s of c.students"
            >{{ s.username }}
            <button
              style="border:0;background:transparent;color:#718096;padding:0"
              (click)="removeStudent(c, s)"
            >
              ×
            </button></span
          >
        </div>
      </div>
    </div>
    <div *ngIf="!classes.length" class="card">
      <div class="empty">
        <h3>No classes</h3>
        <p>Create a class and add your students.</p>
      </div>
    </div>`,
})
export class ClassesComponent {
  classes: Classroom[] = [];
  students: UserResponse[] = [];
  showForm = false;
  editing: Classroom | null = null;
  selected: Record<number, number> = {};
  readonly form: FormGroup;
  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TeacherApiService,
  ) {
    this.form = this.fb.nonNullable.group({
      name: ["", [Validators.required, Validators.maxLength(150)]],
    });
  }
  ngOnInit() {
    this.load();
  }
  load() {
    this.api.classes().subscribe((c) => (this.classes = c));
    this.api.students().subscribe((s) => (this.students = s));
  }
  available(c: Classroom) {
    const ids = new Set(c.students.map((s) => s.id));
    return this.students.filter((s) => !ids.has(s.id));
  }
  edit(c: Classroom) {
    this.editing = c;
    this.form.setValue({ name: c.name });
    this.showForm = true;
  }
  save() {
    if (this.form.invalid) return;
    const name = this.form.controls["name"].value.trim();
    const req = this.editing
      ? this.api.updateClass(this.editing.id, name)
      : this.api.createClass(name);
    req.subscribe((c) => {
      this.classes = this.editing
        ? this.classes.map((x) => (x.id === c.id ? c : x))
        : [c, ...this.classes];
      this.form.reset();
      this.editing = null;
      this.showForm = false;
    });
  }
  removeClass(c: Classroom) {
    if (!confirm(`Delete class “${c.name}”?`)) return;
    this.api
      .deleteClass(c.id)
      .subscribe(
        () => (this.classes = this.classes.filter((x) => x.id !== c.id)),
      );
  }
  add(c: Classroom) {
    const studentId = this.selected[c.id];
    if (!studentId) return;
    this.api.addStudent(c.id, studentId).subscribe((updated) => {
      this.classes = this.classes.map((x) =>
        x.id === updated.id ? updated : x,
      );
      delete this.selected[c.id];
    });
  }
  removeStudent(c: Classroom, s: UserResponse) {
    this.api
      .removeStudent(c.id, s.id)
      .subscribe(
        (updated) =>
          (this.classes = this.classes.map((x) =>
            x.id === updated.id ? updated : x,
          )),
      );
  }
}
