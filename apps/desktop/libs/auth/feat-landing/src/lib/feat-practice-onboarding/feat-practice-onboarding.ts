import { Component, EventEmitter, Output } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { HlmError, HlmFormField, HlmHint } from '@spartan-ng/helm/form-field';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmButton } from '@spartan-ng/helm/button';

@Component({
  selector: 'lib-feat-practice-onboarding',
  imports: [
    ReactiveFormsModule,
    HlmFormField,
    HlmInput,
    HlmHint,
    HlmError,
    HlmButton,
  ],
  templateUrl: './feat-practice-onboarding.html',
  styleUrl: './feat-practice-onboarding.css',
})
export class FeatPracticeOnboarding {
  @Output() completed = new EventEmitter<void>();

  // private readonly http = inject(HttpClient);

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
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    // try {
    //   const payload: PracticePayload = this.form.getRawValue();
    //
    //   // Adjust to your actual BFF route.
    //   // Recommended: POST /api/practice/create or /api/practice
    //   await firstValueFrom(
    //     this.http.post('/api/practice', payload, { withCredentials: true }),
    //   );
    //
    //   toast.success('Practice saved', { description: 'You’re all set.' });
    //   this.completed.emit();
    // } catch (e: any) {
    //   toast.error('Could not save practice', {
    //     description: e?.message ?? 'Please try again.',
    //   });
    // } finally {
    //   this.submitting = false;
    // }
  }
}
