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
import QRCode from 'qrcode';

import { LinksClientService } from '@org/links-data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { LoggerService } from '@org/util';
import { ServerPairingCodeCreateResponse } from '@org/contracts';

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
  private readonly patients = inject(PatientsClientService);
  private readonly log = inject(LoggerService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  private readonly dialogRef = inject<BrnDialogRef<void>>(BrnDialogRef);

  readonly loading = signal(false);
  readonly pairing = signal<ServerPairingCodeCreateResponse | null>(null);
  readonly qrDataUrl = signal<string | null>(null);
  readonly expiresInSeconds = signal<number | null>(null);
  readonly waitingForPatient = signal(false);

  private countdownIntervalId: number | null = null;
  private watchIntervalId: number | null = null;
  private watchInFlight = false;
  private baselineApprovedCount: number | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.clearIntervals();
    });

    // Generate immediately on open.
    void this.generate();
  }

  async generate(): Promise<void> {
    if (this.loading()) return;

    this.loading.set(true);
    this.links.clearError();

    try {
      await this.ensureBaselineApprovedCount();

      const resp = await this.links.createPairingCode();
      this.pairing.set(resp);

      const qrText = (resp.qrText ?? resp.code ?? '').trim();
      this.qrDataUrl.set(
        qrText
          ? await QRCode.toDataURL(qrText, { width: 240, margin: 1 })
          : null,
      );

      this.startCountdown(resp.expiresAt);
      this.startLinkWatch();
      this.cdr.markForCheck();
    } catch (e) {
      this.log.error('Create pairing code failed', { error: e });
      toast.error('Could not generate code', {
        description: 'Please try again.',
      });
    } finally {
      this.loading.set(false);
      this.cdr.markForCheck();
    }
  }

  copyCode(): void {
    const code = this.pairing()?.code?.trim();
    if (!code) return;

    void navigator.clipboard
      .writeText(code)
      .then(() => toast.success('Code copied'))
      .catch(() => toast.error('Could not copy code'));
  }

  close(): void {
    this.waitingForPatient.set(false);
    this.clearIntervals();
    this.dialogRef.close();
  }

  private startCountdown(expiresAt?: string): void {
    if (this.countdownIntervalId != null) {
      window.clearInterval(this.countdownIntervalId);
      this.countdownIntervalId = null;
    }

    const expiryMs = expiresAt ? new Date(expiresAt).getTime() : NaN;
    if (!Number.isFinite(expiryMs)) {
      this.expiresInSeconds.set(null);
      return;
    }

    const update = () => {
      const remaining = Math.max(0, Math.ceil((expiryMs - Date.now()) / 1000));
      this.expiresInSeconds.set(remaining);

      if (remaining === 0) {
        this.waitingForPatient.set(false);
        if (this.watchIntervalId != null) {
          window.clearInterval(this.watchIntervalId);
          this.watchIntervalId = null;
        }
      }

      this.cdr.markForCheck();
    };

    update();

    // Run outside Angular but re-enter for updates.
    this.zone.runOutsideAngular(() => {
      this.countdownIntervalId = window.setInterval(() => {
        this.zone.run(update);
      }, 250);
    });
  }

  private clearIntervals(): void {
    if (this.countdownIntervalId != null) {
      window.clearInterval(this.countdownIntervalId);
      this.countdownIntervalId = null;
    }

    if (this.watchIntervalId != null) {
      window.clearInterval(this.watchIntervalId);
      this.watchIntervalId = null;
    }
  }

  private async ensureBaselineApprovedCount(): Promise<void> {
    if (this.baselineApprovedCount != null) return;

    try {
      const resp = await this.patients.getPatientsResponse();
      this.baselineApprovedCount = resp.patients?.length ?? 0;
    } catch (e) {
      this.log.warn('Could not load baseline patients count', { error: e });
      this.baselineApprovedCount = 0;
    }
  }

  private startLinkWatch(): void {
    if (this.watchIntervalId != null) {
      window.clearInterval(this.watchIntervalId);
      this.watchIntervalId = null;
    }

    // Poll the doctor patient list; redeeming a pairing code creates an approved link.
    this.waitingForPatient.set(true);
    this.zone.runOutsideAngular(() => {
      this.watchIntervalId = window.setInterval(() => {
        if (this.watchInFlight) return;
        if (this.baselineApprovedCount == null) return;

        this.watchInFlight = true;
        void this.checkForApprovedIncrease().finally(() => {
          this.watchInFlight = false;
        });
      }, 1000);
    });
  }

  private async checkForApprovedIncrease(): Promise<void> {
    const baseline = this.baselineApprovedCount;
    if (baseline == null) return;

    try {
      const resp = await this.patients.getPatientsResponse();
      const approvedCount = resp.patients?.length ?? 0;
      if (approvedCount > baseline) {
        this.zone.run(() => {
          this.waitingForPatient.set(false);
          toast.success('Patient connected');
          this.close();
        });
      }
    } catch (e) {
      // Non-fatal; keep polling until expiry/dismiss.
      this.log.warn('Polling patients failed', { error: e });
    }
  }
}
