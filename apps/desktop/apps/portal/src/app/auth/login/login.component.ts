import { NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginRequestDto, LoginResponseDto } from '@org/models';
import { ButtonComponent, CardComponent, FormFieldComponent } from '@org/portal-ui';
import { ApiClient, ApiError } from '@org/util';

@Component({
  selector: 'portal-login',
  standalone: true,
  imports: [
    ButtonComponent,
    CardComponent,
    FormFieldComponent,
    NgIf,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly api = new ApiClient();
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly emailId = 'auth-login-email';
  readonly passwordId = 'auth-login-password';

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  loading = false;
  formError = '';

  get emailError(): string {
    const control = this.form.controls.email;
    if (!control.touched) {
      return '';
    }
    if (control.hasError('required')) {
      return 'Email is required.';
    }
    if (control.hasError('email')) {
      return 'Enter a valid email.';
    }
    return '';
  }

  get passwordError(): string {
    const control = this.form.controls.password;
    if (!control.touched) {
      return '';
    }
    if (control.hasError('required')) {
      return 'Password is required.';
    }
    if (control.hasError('minlength')) {
      return 'Use at least 8 characters.';
    }
    return '';
  }

  get emailDescribedBy(): string | null {
    const ids = ['auth-login-email-hint'];
    if (this.emailError) {
      ids.push('auth-login-email-error');
    }
    return ids.join(' ');
  }

  get passwordDescribedBy(): string | null {
    const ids = ['auth-login-password-hint'];
    if (this.passwordError) {
      ids.push('auth-login-password-error');
    }
    return ids.join(' ');
  }

  async onSubmit(): Promise<void> {
    this.formError = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    try {
      const payload: LoginRequestDto = this.form.getRawValue();
      await this.api.post<LoginResponseDto, LoginRequestDto>(
        '/api/auth/login',
        payload
      );
      await this.router.navigate(['/patients']);
    } catch (error) {
      this.formError = this.mapErrorMessage(error);
    } finally {
      this.loading = false;
    }
  }

  private mapErrorMessage(error: unknown): string {
    if (error instanceof ApiError) {
      const payloadMessage = this.readPayloadMessage(error.payload);
      if (payloadMessage) {
        return payloadMessage;
      }

      switch (error.status) {
        case 400:
          return 'Check your email and password, then try again.';
        case 401:
          return 'That email and password do not match our records.';
        case 403:
          return 'Your account is pending approval.';
        default:
          return 'We could not sign you in. Please try again.';
      }
    }

    return 'We could not sign you in. Please try again.';
  }

  private readPayloadMessage(payload: unknown): string | null {
    if (!payload || typeof payload !== 'object') {
      return null;
    }

    const message = (payload as { message?: unknown }).message;
    if (typeof message === 'string' && message.trim().length > 0) {
      return message;
    }

    const error = (payload as { error?: unknown }).error;
    if (typeof error === 'string' && error.trim().length > 0) {
      return error;
    }

    return null;
  }
}
