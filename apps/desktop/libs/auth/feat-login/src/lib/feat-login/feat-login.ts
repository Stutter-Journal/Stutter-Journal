import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  inject,
  Output,
} from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import {
  FormBuilder,
  FormControl,
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
import { HlmSeparator } from '@spartan-ng/helm/separator';

@Component({
  selector: 'lib-feat-login',
  imports: [
    CommonModule,
    NgOptimizedImage,
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
    HlmSeparator,
  ],
  templateUrl: './feat-login.html',
  styleUrl: './feat-login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatLogin {
  @Output() switchToRegister = new EventEmitter<void>();

  readonly email = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.email],
  });

  signInWithEmail(): void {
    if (this.email.invalid) return;
    console.log('Email sign-in:', this.email.value);
  }

  signInWithGithub(): void {
    console.log('GitHub sign-in');
  }

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    remember: [true],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    console.log('Login payload', this.form.value);
  }
}
