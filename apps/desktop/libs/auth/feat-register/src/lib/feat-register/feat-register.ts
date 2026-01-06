import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  inject,
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
import { AuthClientService } from '@org/auth-data-access';
import { HlmError, HlmFormField, HlmHint } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';
import { toast } from 'ngx-sonner';

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

  readonly auth = inject(AuthClientService);

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

  errorMessage(): string | null {
    const err = this.auth.error();
    return err?.message ?? null;
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.auth.clearError();

    const { acceptTerms, ...raw } = this.form.getRawValue();

    try {
      await this.auth.register({
        email: raw.email,
        password: raw.password,
        displayName: raw.firstName + raw.lastName,
      });

      toast.success('Account created', {
        description: 'Welcome! You can continue to set up your practice.',
      });

      // Next step: trigger onboarding (you’ll hook this later)
      // e.g. emit event, navigate, or flip cascade state
    } catch (e: any) {
      toast.error('Registration failed', {
        description: e?.message ?? 'Please try again.',
      });
    }
  }
}
