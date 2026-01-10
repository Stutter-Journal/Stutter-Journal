import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { LinksClientService } from '@org/links-data-access';
import { LoggerService } from '@org/util';
import { ServerLinkResponse, ServerPatientDTO } from '@org/contracts';

import { toast } from 'ngx-sonner';

import { BrnDialogClose, BrnDialogRef } from '@spartan-ng/brain/dialog';
import {
  HlmDialogDescription,
  HlmDialogFooter,
  HlmDialogHeader,
  HlmDialogTitle,
} from '@spartan-ng/helm/dialog';

import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmFormFieldImports } from '@spartan-ng/helm/form-field';

@Component({
  selector: 'lib-add-patient',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,

    // spartan bundles
    HlmButtonImports,
    HlmInputImports,
    HlmFormFieldImports,

    // dialog primitives for dynamic content
    HlmDialogHeader,
    HlmDialogTitle,
    HlmDialogDescription,
    HlmDialogFooter,
    BrnDialogClose,
  ],
  host: {
    class: 'flex flex-col gap-5', // recommended pattern in dynamic content example
  },
  templateUrl: './add-patient.html',
  styleUrl: './add-patient.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddPatient {
  private readonly links = inject(LinksClientService);
  private readonly log = inject(LoggerService);

  // ✅ Correctly typed: we close with ServerPatientDTO (or undefined)
  private readonly dialogRef =
    inject<BrnDialogRef<ServerPatientDTO | undefined>>(BrnDialogRef);

  readonly form = new FormGroup({
    displayName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    patientEmail: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    patientCode: new FormControl('', { nonNullable: true }),
  });

  readonly submitting = signal(false);
  readonly loading = this.links.loading;

  async submit(): Promise<void> {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.links.clearError();

    const raw = this.form.getRawValue();
    const payload = {
      displayName: raw.displayName.trim(),
      patientEmail: raw.patientEmail.trim(),
      patientCode: raw.patientCode.trim() || undefined,
    };

    try {
      const response: ServerLinkResponse =
        await this.links.invitePatient(payload);

      // Prefer server-returned patient. If missing, fail softly but still return something consistent.
      const patient = response.patient;
      if (!patient) {
        toast.success('Invitation sent');
        this.dialogRef.close(undefined);
        return;
      }

      toast.success('Invitation sent', {
        description: patient.displayName
          ? `${patient.displayName} can connect via email or code.`
          : 'The patient can connect via email or code.',
      });

      this.dialogRef.close(patient);
    } catch (err: unknown) {
      const message =
        (typeof err === 'object' &&
          err !== null &&
          'message' in err &&
          typeof (err as { message?: unknown }).message === 'string' &&
          (err as { message: string }).message) ||
        (typeof err === 'object' &&
          err !== null &&
          'error' in err &&
          typeof (err as { error?: unknown }).error === 'object' &&
          (err as { error?: { message?: unknown } }).error?.message &&
          typeof (err as { error: { message: unknown } }).error.message ===
            'string' &&
          (err as { error: { message: string } }).error.message) ||
        'Please try again.';

      this.log.error('Invite patient failed', { error: err });
      toast.error('Could not add patient');
    } finally {
      this.submitting.set(false);
    }
  }
}
