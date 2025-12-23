import { NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonComponent, CardComponent, FormFieldComponent } from '@org/portal-ui';
import { AuthService } from '../auth.service';

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
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

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
      const payload = this.form.getRawValue();
      const errorMessage = await this.auth.register(payload);
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
