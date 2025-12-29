import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  ButtonComponent,
  CardComponent,
  FormFieldComponent,
} from '@org/portal-ui';
import { AuthService } from '../auth.service';

@Component({
  selector: 'portal-login',
  standalone: true,
  imports: [
    ButtonComponent,
    CardComponent,
    FormFieldComponent,
    ReactiveFormsModule,
    RouterLink,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

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
      const { email, password } = this.form.getRawValue();
      const errorMessage = await this.auth.login(email, password);
      if (errorMessage) {
        this.formError = errorMessage;
        return;
      }

      await this.router.navigate(['/onboarding']);
    } finally {
      this.loading = false;
    }
  }
}
