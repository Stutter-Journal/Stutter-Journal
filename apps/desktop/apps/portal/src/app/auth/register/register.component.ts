import { NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RegisterRequestDto, RegisterResponseDto } from '@org/models';
import { ButtonComponent, CardComponent, FormFieldComponent } from '@org/portal-ui';
import { ApiClient, ApiError } from '@org/util';

@Component({
  selector: 'portal-register',
  standalone: true,
  imports: [
    ButtonComponent,
    CardComponent,
    FormFieldComponent,
    NgIf,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly api = new ApiClient();
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly firstNameId = 'auth-register-first-name';
  readonly lastNameId = 'auth-register-last-name';
  readonly emailId = 'auth-register-email';
  readonly passwordId = 'auth-register-password';

  readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  loading = false;
  formError = '';

  get firstNameError(): string {
    const control = this.form.controls.firstName;
    if (!control.touched) {
      return '';
    }
    if (control.hasError('required')) {
      return 'First name is required.';
    }
    if (control.hasError('minlength')) {
      return 'Enter at least 2 characters.';
    }
    return '';
  }

  get lastNameError(): string {
    const control = this.form.controls.lastName;
    if (!control.touched) {
      return '';
    }
    if (control.hasError('required')) {
      return 'Last name is required.';
    }
    if (control.hasError('minlength')) {
      return 'Enter at least 2 characters.';
    }
    return '';
  }

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

  get firstNameDescribedBy(): string | null {
    const ids = ['auth-register-first-name-hint'];
    if (this.firstNameError) {
      ids.push('auth-register-first-name-error');
    }
    return ids.join(' ');
  }

  get lastNameDescribedBy(): string | null {
    const ids = ['auth-register-last-name-hint'];
    if (this.lastNameError) {
      ids.push('auth-register-last-name-error');
    }
    return ids.join(' ');
  }

  get emailDescribedBy(): string | null {
    const ids = ['auth-register-email-hint'];
    if (this.emailError) {
      ids.push('auth-register-email-error');
    }
    return ids.join(' ');
  }

  get passwordDescribedBy(): string | null {
    const ids = ['auth-register-password-hint'];
    if (this.passwordError) {
      ids.push('auth-register-password-error');
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
      const payload: RegisterRequestDto = this.form.getRawValue();
      await this.api.post<RegisterResponseDto, RegisterRequestDto>(
        '/api/auth/register',
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
          return 'Check the form details and try again.';
        case 409:
          return 'An account with that email already exists.';
        default:
          return 'We could not create your account. Please try again.';
      }
    }

    return 'We could not create your account. Please try again.';
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
