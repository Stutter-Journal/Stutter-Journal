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
  private readonly log = inject(LoggerService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  private readonly dialogRef = inject<BrnDialogRef<void>>(BrnDialogRef);

  readonly loading = signal(false);
  readonly pairing = signal<ServerPairingCodeCreateResponse | null>(null);
  readonly qrDataUrl = signal<string | null>(null);
  readonly expiresInSeconds = signal<number | null>(null);

  private intervalId: number | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => {
      if (this.intervalId != null) {
        window.clearInterval(this.intervalId);
      }
    });

    // Generate immediately on open.
    void this.generate();
  }

  async generate(): Promise<void> {
    if (this.loading()) return;

    this.loading.set(true);
    this.links.clearError();

    try {
      const resp = await this.links.createPairingCode();
      this.pairing.set(resp);

      const qrText = (resp.qrText ?? resp.code ?? '').trim();
      this.qrDataUrl.set(
        qrText
          ? await QRCode.toDataURL(qrText, { width: 240, margin: 1 })
          : null,
      );

      this.startCountdown(resp.expiresAt);
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
    this.dialogRef.close();
  }

  private startCountdown(expiresAt?: string): void {
    if (this.intervalId != null) {
      window.clearInterval(this.intervalId);
      this.intervalId = null;
    }

    const expiryMs = expiresAt ? new Date(expiresAt).getTime() : NaN;
    if (!Number.isFinite(expiryMs)) {
      this.expiresInSeconds.set(null);
      return;
    }

    const update = () => {
      const remaining = Math.max(0, Math.ceil((expiryMs - Date.now()) / 1000));
      this.expiresInSeconds.set(remaining);
      this.cdr.markForCheck();
    };

    update();

    // Run outside Angular but re-enter for updates.
    this.zone.runOutsideAngular(() => {
      this.intervalId = window.setInterval(() => {
        this.zone.run(update);
      }, 250);
    });
  }
}
