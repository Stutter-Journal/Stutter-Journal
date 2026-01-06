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
import { CheckboxModule } from 'primeng/checkbox';
import { HlmError, HlmFormField, HlmHint } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';

@Component({
  selector: 'lib-feat-register',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    CheckboxModule,
    HlmFormField,
    HlmInput,
    HlmError,
    HlmHint,
    HlmButton,
  ],
  templateUrl: './feat-register.html',
  styleUrl: './feat-register.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatRegister {
  @Output() switchToLogin = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<{
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }>();

  submitting = false;

  readonly form = new FormGroup({
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8)],
    }),
    acceptTerms: new FormControl(false, {
      nonNullable: true,
      validators: [Validators.requiredTrue],
    }),
  });

  submit(): void {
    if (this.form.invalid) return;

    this.submitting = true;

    // TODO: Integrate that acceptTerms somehow
    const { acceptTerms, ...payload } = this.form.getRawValue();
    this.submitted.emit(payload);
  }
}
