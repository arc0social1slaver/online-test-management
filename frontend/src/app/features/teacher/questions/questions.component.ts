import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { TeacherApiService } from "../../../core/services/teacher-api.service";
import { Question, Tag } from "../../../core/models/api.models";

@Component({
  standalone: true,
  selector: "app-questions",
  imports: [CommonModule, ReactiveFormsModule],
  template: ` <div class="page-title">
      <div>
        <h1>Questions</h1>
        <p>Build a four-choice question bank with one or more tags.</p>
      </div>
      <button class="btn btn-primary" (click)="startNew()">
        + {{ editing ? "New question" : "Create question" }}
      </button>
    </div>
    <div class="grid grid-2">
      <div class="card" *ngIf="showForm">
        <h2 style="margin:0 0 16px;font-size:18px">
          {{ editing ? "Edit question" : "New question" }}
        </h2>
        <div *ngIf="error" class="alert" style="margin-bottom:12px">
          {{ error }}
        </div>
        <form [formGroup]="form" (ngSubmit)="save()" class="stack">
          <div class="field">
            <label>Question text</label
            ><textarea formControlName="text"></textarea>
          </div>
          <div class="form-grid">
            <div class="field">
              <label>Choice A</label><input formControlName="choiceA" />
            </div>
            <div class="field">
              <label>Choice B</label><input formControlName="choiceB" />
            </div>
            <div class="field">
              <label>Choice C</label><input formControlName="choiceC" />
            </div>
            <div class="field">
              <label>Choice D</label><input formControlName="choiceD" />
            </div>
          </div>
          <div class="field">
            <label>Correct answer</label
            ><select formControlName="correctIndex">
              <option [ngValue]="0">A</option>
              <option [ngValue]="1">B</option>
              <option [ngValue]="2">C</option>
              <option [ngValue]="3">D</option>
            </select>
          </div>
          <div class="field">
            <label>Tags</label>
            <div class="inline">
              <input
                #newTagInput
                placeholder="Create an additional tag"
                (keyup.enter)="
                  createTag(newTagInput.value); newTagInput.value = ''
                "
                style="max-width:260px"
              /><button
                type="button"
                class="btn btn-sm"
                (click)="createTag(newTagInput.value); newTagInput.value = ''"
              >
                Add tag
              </button>
            </div>
            <div class="checkbox-grid">
              <label class="checkbox-pill" *ngFor="let t of tags"
                ><input
                  type="checkbox"
                  [checked]="tagIds.value.includes(t.id)"
                  (change)="toggleTag(t.id, $any($event.target).checked)"
                />{{ t.name }}</label
              >
            </div>
          </div>
          <div class="actions">
            <button type="submit" class="btn btn-primary" [disabled]="saving">
              {{ saving ? "Saving…" : "Save question" }}</button
            ><button type="button" class="btn" (click)="cancel()">
              Cancel
            </button>
          </div>
        </form>
      </div>
      <div class="card">
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th>Question</th>
                <th>Tags</th>
                <th>Correct</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let q of questions">
                <td>
                  <strong>{{ q.text }}</strong>
                  <div class="muted">{{ q.choices.length }} choices</div>
                </td>
                <td>
                  <div class="tag-list">
                    <span class="tag" *ngFor="let t of q.tags">{{
                      t.name
                    }}</span>
                  </div>
                </td>
                <td>{{ letters[q.correctIndex] }}</td>
                <td>
                  <div class="actions">
                    <button class="btn btn-sm" (click)="edit(q)">Edit</button
                    ><button class="btn btn-sm btn-danger" (click)="delete(q)">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div *ngIf="!questions.length" class="empty">
          <h3>No questions</h3>
          <p>Add tagged questions before generating a test.</p>
        </div>
      </div>
    </div>`,
})
export class QuestionsComponent {
  questions: Question[] = [];
  tags: Tag[] = [];
  showForm = false;
  editing: Question | null = null;
  saving = false;
  error = "";
  letters = ["A", "B", "C", "D"];
  readonly form: FormGroup;
  get tagIds() {
    return this.form.controls["tagIds"];
  }
  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TeacherApiService,
  ) {
    this.form = this.fb.group({
      text: this.fb.nonNullable.control("", Validators.required),
      choiceA: this.fb.nonNullable.control("", Validators.required),
      choiceB: this.fb.nonNullable.control("", Validators.required),
      choiceC: this.fb.nonNullable.control("", Validators.required),
      choiceD: this.fb.nonNullable.control("", Validators.required),
      correctIndex: this.fb.nonNullable.control(0, Validators.required),
      tagIds: this.fb.nonNullable.control<number[]>([], Validators.required),
    });
  }
  ngOnInit() {
    this.api.questions().subscribe((q) => (this.questions = q));
    this.api.tags().subscribe((t) => (this.tags = t));
  }
  startNew() {
    this.editing = null;
    this.form.reset({
      text: "",
      choiceA: "",
      choiceB: "",
      choiceC: "",
      choiceD: "",
      correctIndex: 0,
      tagIds: [],
    });
    this.error = "";
    this.showForm = true;
  }
  cancel() {
    this.showForm = false;
    this.editing = null;
  }
  toggleTag(id: number, checked: boolean) {
    const ids = new Set(this.tagIds.value);
    checked ? ids.add(id) : ids.delete(id);
    this.tagIds.setValue([...ids]);
  }
  edit(q: Question) {
    this.editing = q;
    this.form.setValue({
      text: q.text,
      choiceA: q.choices[0],
      choiceB: q.choices[1],
      choiceC: q.choices[2],
      choiceD: q.choices[3],
      correctIndex: q.correctIndex,
      tagIds: q.tags.map((t) => t.id),
    });
    this.showForm = true;
  }
  save() {
    if (this.form.invalid || !this.tagIds.value.length) {
      this.form.markAllAsTouched();
      this.error = "Select at least one tag.";
      return;
    }
    this.saving = true;
    this.error = "";
    const v = this.form.getRawValue();
    const body = { ...v };
    const req = this.editing
      ? this.api.updateQuestion(this.editing.id, body)
      : this.api.createQuestion(body);
    req.subscribe({
      next: (q) => {
        this.questions = this.editing
          ? this.questions.map((x) => (x.id === q.id ? q : x))
          : [q, ...this.questions];
        this.cancel();
      },
      error: (e) =>
        (this.error = e?.error?.message || "Could not save question."),
      complete: () => (this.saving = false),
    });
  }
  delete(q: Question) {
    if (
      !confirm(
        "Delete this question? Existing tests keep their saved snapshot.",
      )
    )
      return;
    this.api
      .deleteQuestion(q.id)
      .subscribe(
        () => (this.questions = this.questions.filter((x) => x.id !== q.id)),
      );
  }
  createTag(name: string) {
    const value = name.trim();
    if (!value) return;
    this.api.createTag(value).subscribe({
      next: (t) => {
        this.tags = [...this.tags, t].sort((a, b) =>
          a.name.localeCompare(b.name),
        );
        this.toggleTag(t.id, true);
      },
      error: (e) => (this.error = e?.error?.message || "Could not create tag."),
    });
  }
}
