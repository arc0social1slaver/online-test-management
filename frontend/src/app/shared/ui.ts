import { Component, Input } from "@angular/core";

@Component({
  standalone: true,
  selector: "app-empty",
  template: `<div class="empty">
    <div class="empty-icon">{{ icon }}</div>
    <h3>{{ title }}</h3>
    <p>{{ text }}</p>
  </div>`,
})
export class EmptyStateComponent {
  @Input() title = "No data";
  @Input() text = "";
  @Input() icon = "◌";
}
