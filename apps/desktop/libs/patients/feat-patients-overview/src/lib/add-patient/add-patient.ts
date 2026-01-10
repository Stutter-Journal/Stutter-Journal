import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  inject,
  NgZone,
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
import { createRequestFlow, LoggerService } from '@org/util';
import {
  ServerLinkResponse,
  ServerPatientDTO,
  ServerLinkInviteRequest,
} from '@org/contracts';

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
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

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

  private readonly inviteFlow = createRequestFlow<
    ServerLinkInviteRequest,
    ServerLinkResponse
  >({
    request: async (payload) => {
      this.links.clearError();
      return await this.links.invitePatient(payload);
    },
  });

  async submit(): Promise<void> {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload = {
      displayName: raw.displayName.trim(),
      patientEmail: raw.patientEmail.trim(),
      patientCode: raw.patientCode.trim() || undefined,
    };

    this.inviteFlow.submit(payload);
  }

  constructor() {
    this.inviteFlow.start();

    let prev = this.inviteFlow.getSnapshot();
    const unsubscribe = this.inviteFlow.subscribe((curr) => {
      // The flow emits outside Angular; re-enter the zone so OnPush views update.
      this.zone.run(() => {
        this.submitting.set(curr.state === 'submitting');

        if (prev.state !== 'success' && curr.state === 'success') {
          const response = curr.result;
          const patient = response?.patient;

          toast.success('Invitation sent', {
            description: patient?.displayName
              ? `${patient.displayName} can connect via email or code.`
              : 'The patient can connect via email or code.',
          });

          this.dialogRef.close(patient);
        }

        if (prev.state !== 'failure' && curr.state === 'failure') {
          toast.error('Could not add patient', {
            description: curr.error ?? 'Please try again.',
          });
          this.log.error('Invite patient failed', { error: curr.error });
        }

        prev = curr;
        this.cdr.markForCheck();
      });
    });

    this.destroyRef.onDestroy(() => {
      unsubscribe();
      this.inviteFlow.stop();
    });
  }
}
