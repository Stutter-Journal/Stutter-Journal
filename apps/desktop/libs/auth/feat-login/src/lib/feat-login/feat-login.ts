import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  EventEmitter,
  inject,
  NgZone,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { HlmError, HlmFormField } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';
import { toast } from 'ngx-sonner';
import { AuthClientService } from '@org/auth-data-access';
import { createRequestFlow, LoggerService } from '@org/util';

@Component({
  selector: 'lib-feat-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
  private readonly log = inject(LoggerService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  @Output() switchToRegister = new EventEmitter<void>();
  @Output() authed = new EventEmitter<void>();

  private readonly loginFlow = createRequestFlow<{
    email: string;
    password: string;
  }>({
    request: async ({ email, password }) => {
      await this.auth.login({ email, password });
    },
    errorMessage: (err) =>
      err instanceof Error ? err.message : 'Login failed',
  });

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
    if (this.form.invalid || this.submitting) {
      this.log.debug('Login submit skipped', {
        submitting: this.submitting,
        valid: this.form.valid,
      });
      return;
    }

    const { email, password } = this.form.getRawValue();
    this.log.info('Attempting login', { email });

    // Drive the UI with a state machine transition instead of a manual try/catch.
    this.loginFlow.submit({ email, password });
  }

  constructor() {
    this.loginFlow.start();

    let prev = this.loginFlow.getSnapshot();
    const unsubscribe = this.loginFlow.subscribe((curr) => {
      // The flow emits outside Angular; re-enter the zone so OnPush views update.
      this.zone.run(() => {
        this.submitting = curr.state === 'submitting';

        if (prev.state !== 'success' && curr.state === 'success') {
          toast.success('Welcome back');
          this.log.info('Login succeeded', { email: curr.input?.email });
          this.authed.emit();
        }

        if (prev.state !== 'failure' && curr.state === 'failure') {
          const description = curr.error ?? 'Please check your credentials';
          toast.error('Login failed', { description });
          this.log.error('Login failed', { error: description });
        }

        prev = curr;
        this.cdr.markForCheck();
      });
    });

    this.destroyRef.onDestroy(() => {
      unsubscribe();
      this.loginFlow.stop();
    });
  }
}
