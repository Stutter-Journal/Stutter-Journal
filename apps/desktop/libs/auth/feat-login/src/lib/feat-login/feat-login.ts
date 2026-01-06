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
import { DividerModule } from 'primeng/divider';
import { CheckboxModule } from 'primeng/checkbox';
import { HlmError, HlmFormField } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';
import { toast } from 'ngx-sonner';
import { AuthClientService } from '@org/auth-data-access';

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
  private readonly auth = inject(AuthClientService);

  @Output() switchToRegister = new EventEmitter<void>();
  @Output() authed = new EventEmitter<void>();

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

  async submit(): Promise<void> {
    if (this.form.invalid || this.submitting) return;

    this.submitting = true;
    try {
      const { email, password } = this.form.getRawValue();

      await this.auth.login({ email, password });

      toast.success('Welcome back');

      // 🔑 Let cascade / landing handle next step
      this.authed.emit();
    } catch (err: any) {
      toast.error('Login failed', {
        description: err?.message ?? 'Please check your credentials',
      });
    } finally {
      this.submitting = false;
    }
  }
}
