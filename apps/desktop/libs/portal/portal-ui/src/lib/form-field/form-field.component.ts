import { NgIf } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'lib-form-field',
  standalone: true,
  imports: [NgIf],
  templateUrl: './form-field.component.html',
  styleUrl: './form-field.component.css',
})
export class FormFieldComponent {
  private static nextId = 0;
  private readonly internalId = `form-field-${FormFieldComponent.nextId++}`;

  @Input() label = '';
  @Input() hint = '';
  @Input() error = '';
  @Input() required = false;
  @Input() fieldId?: string;

  get resolvedId(): string {
    return this.fieldId ?? this.internalId;
  }

  get hintId(): string {
    return `${this.resolvedId}-hint`;
  }

  get errorId(): string {
    return `${this.resolvedId}-error`;
  }
}
