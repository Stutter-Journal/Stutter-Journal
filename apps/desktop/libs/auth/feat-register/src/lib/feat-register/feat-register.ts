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
import { AuthClientService } from '@org/auth-data-access';
import { HlmError, HlmFormField, HlmHint } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';
import { toast } from 'ngx-sonner';
import { LoggerService } from '@org/util';

@Component({
  selector: 'lib-feat-register',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HlmFormField,
    HlmInput,
    HlmError,
    HlmHint,
    HlmButton,
  ],
  templateUrl: './feat-register.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatRegister {
  @Output() switchToLogin = new EventEmitter<void>();
  @Output() authed = new EventEmitter<void>();

  readonly auth = inject(AuthClientService);
  private readonly log = inject(LoggerService);

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
      this.log.debug('Registration form invalid', {
        errors: this.form.errors,
        status: this.form.status,
      });
      this.form.markAllAsTouched();
      return;
    }

    this.log.info('Submitting registration');
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

      this.log.info('Registration succeeded', { email: raw.email });

      this.authed.emit();
    } catch (e: any) {
      this.log.error('Registration failed', { error: e });
      toast.error('Registration failed', {
        description: e?.message ?? 'Please try again.',
      });
    }
  }
}
