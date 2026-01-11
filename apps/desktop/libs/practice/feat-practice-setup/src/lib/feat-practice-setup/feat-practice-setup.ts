import { Component, EventEmitter, inject, Output } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { PracticeClientService } from '@org/practice-data-access';
import { HlmButton } from '@spartan-ng/helm/button';
import { HlmError, HlmFormField, HlmHint } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { toast } from 'ngx-sonner';
import { LoggerService } from '@org/util';

@Component({
  selector: 'lib-feat-practice-setup',
  imports: [
    FormsModule,
    HlmButton,
    HlmError,
    HlmFormField,
    HlmHint,
    HlmInput,
    ReactiveFormsModule,
  ],
  templateUrl: './feat-practice-setup.html',
})
export class FeatPracticeSetup {
  @Output() completed = new EventEmitter<void>();

  readonly practice = inject(PracticeClientService);
  private readonly log = inject(LoggerService);

  submitting = false;

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    address: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.log.debug('Practice setup form invalid', {
        status: this.form.status,
        errors: this.form.errors,
      });
      this.form.markAllAsTouched();
      return;
    }

    if (this.submitting) {
      this.log.debug('Practice setup submit already in-flight');
      return;
    }

    this.log.info('Submitting practice setup');
    this.submitting = true;
    try {
      const result = await this.practice.create(this.form.getRawValue());

      this.log.info('Practice created', { id: result?.practice?.id });

      // result.doctor is expected to have practiceId now
      // if (result?.doctor) this.practice.setUser(result.doctor);

      toast.success('Practice created', { description: 'You’re ready to go.' });
      this.completed.emit();
    } catch (e: any) {
      this.log.error('Practice setup failed', { error: e });
      toast.error('Could not save practice', {
        description: e?.message ?? 'Please try again.',
      });
    } finally {
      this.log.debug('Practice setup submit completed');
      this.submitting = false;
    }
  }
}
