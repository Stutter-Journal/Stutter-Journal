import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { CheckboxModule } from 'primeng/checkbox';
import { HlmError, HlmFormField } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';

@Component({
  selector: 'lib-feat-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    DividerModule,
    CheckboxModule,
    HlmFormField,
    HlmInput,
    HlmError,
    HlmButton,
  ],
  templateUrl: './feat-login.html',
  styleUrl: './feat-login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatLogin {
  @Output() switchToRegister = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<{ email: string; password: string }>();
  @Output() authed = new EventEmitter<void>(); // TODO: Add login functionality here

  submitting = false;

  readonly form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  submit(): void {
    if (this.form.invalid) return;

    this.submitting = true;
    this.submitted.emit(this.form.getRawValue());

    // TODO: Implement later on, copy it from the register ts component
  }
}
