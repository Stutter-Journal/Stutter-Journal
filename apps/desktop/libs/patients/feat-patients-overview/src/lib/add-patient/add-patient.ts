import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  computed,
  inject,
  NgZone,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import QRCode from 'qrcode';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, exhaustMap, from, interval, startWith, timer } from 'rxjs';
import { assign, createActor, createMachine, fromPromise } from 'xstate';

import { LinksClientService } from '@org/links-data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { actorSnapshot$, LoggerService } from '@org/util';
import { ServerPairingCodeCreateResponse } from '@org/contracts';

import { toast } from 'ngx-sonner';

import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideCheck } from '@ng-icons/lucide';

import { BrnDialogClose, BrnDialogRef } from '@spartan-ng/brain/dialog';
import {
  HlmDialogDescription,
  HlmDialogFooter,
  HlmDialogHeader,
  HlmDialogTitle,
} from '@spartan-ng/helm/dialog';

import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmIcon } from '@spartan-ng/helm/icon';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmFormFieldImports } from '@spartan-ng/helm/form-field';

type AddPatientState =
  | 'idle'
  | 'generating'
  | 'waiting'
  | 'success'
  | 'expired'
  | 'failure';

type AddPatientEvent =
  | { type: 'GENERATE' }
  | { type: 'CONNECTED' }
  | { type: 'EXPIRED' };

interface AddPatientContext {
  pairing: ServerPairingCodeCreateResponse | null;
  qrDataUrl: string | null;
  expiresAtMs: number | null;
  error?: string;
}

interface AddPatientGenerated {
  pairing: ServerPairingCodeCreateResponse;
  qrDataUrl: string | null;
  expiresAtMs: number | null;
}

@Component({
  selector: 'lib-add-patient',
  standalone: true,
  imports: [
    CommonModule,
    NgIcon,
    // spartan bundles
    HlmButtonImports,
    HlmIcon,
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
  providers: [provideIcons({ lucideCheck })],
})
export class AddPatient {
  private readonly links = inject(LinksClientService);
  private readonly patients = inject(PatientsClientService);
  private readonly log = inject(LoggerService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  private readonly dialogRef = inject<BrnDialogRef<void>>(BrnDialogRef);

  readonly flowState = signal<AddPatientState>('idle');
  readonly loading = computed(() => this.flowState() === 'generating');
  readonly waitingForPatient = computed(() => this.flowState() === 'waiting');
  readonly connected = computed(() => this.flowState() === 'success');
  readonly pairing = signal<ServerPairingCodeCreateResponse | null>(null);
  readonly qrDataUrl = signal<string | null>(null);
  readonly expiresInSeconds = signal<number | null>(null);

  private readonly addPatientActor = createActor(this.buildMachine());
  private countdownSub: Subscription | null = null;
  private watchSub: Subscription | null = null;
  private closeSub: Subscription | null = null;
  private baselineApprovedCount: number | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.stopTimers();
      this.addPatientActor.stop();
    });

    this.addPatientActor.start();
    this.observeState();
    this.generate();
  }

  private observeState(): void {
    let prevState = this.flowState();

    actorSnapshot$(this.addPatientActor)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((snapshot) => {
        const state = snapshot.value as AddPatientState;
        const ctx = snapshot.context as AddPatientContext;

        this.zone.run(() => {
          if (prevState !== state) {
            this.onStateChange(prevState, state, ctx);
          }

          this.flowState.set(state);
          this.pairing.set(ctx.pairing);
          this.qrDataUrl.set(ctx.qrDataUrl);

          prevState = state;
          this.cdr.markForCheck();
        });
      });
  }

  private onStateChange(
    prevState: AddPatientState,
    nextState: AddPatientState,
    ctx: AddPatientContext,
  ): void {
    if (nextState === 'generating') {
      this.stopTimers();
      this.expiresInSeconds.set(null);
      this.cdr.markForCheck();
      return;
    }

    if (prevState === 'waiting' && nextState !== 'waiting') {
      this.stopCountdown();
      this.stopLinkWatch();
    }

    if (nextState === 'waiting') {
      this.startCountdown(ctx.expiresAtMs);
      this.startLinkWatch();
      return;
    }

    if (nextState === 'success') {
      this.stopTimers();
      toast.success('Patient connected');
      this.scheduleClose();
      return;
    }

    if (nextState === 'failure') {
      const description = ctx.error ?? 'Please try again.';
      this.log.error('Create pairing code failed', { error: description });
      toast.error('Could not generate code', { description });
      return;
    }
  }

  private buildMachine() {
    const generatePairing = fromPromise(async () => this.generatePairing());

    return createMachine({
      id: 'addPatient',
      types: {} as { context: AddPatientContext; events: AddPatientEvent },
      initial: 'idle',
      context: {
        pairing: null,
        qrDataUrl: null,
        expiresAtMs: null,
        error: undefined,
      },
      states: {
        idle: {
          on: { GENERATE: 'generating' },
        },
        generating: {
          entry: assign(() => ({
            pairing: null,
            qrDataUrl: null,
            expiresAtMs: null,
            error: undefined,
          })),
          invoke: {
            src: generatePairing,
            onDone: {
              target: 'waiting',
              actions: assign(({ event }) => ({
                pairing: (event as { output: AddPatientGenerated }).output
                  .pairing,
                qrDataUrl: (event as { output: AddPatientGenerated }).output
                  .qrDataUrl,
                expiresAtMs: (event as { output: AddPatientGenerated }).output
                  .expiresAtMs,
                error: undefined,
              })),
            },
            onError: {
              target: 'failure',
              actions: assign(({ event }) => ({
                error: this.formatError((event as { error?: unknown }).error),
              })),
            },
          },
        },
        waiting: {
          on: {
            CONNECTED: 'success',
            EXPIRED: 'expired',
            GENERATE: 'generating',
          },
        },
        success: {
          on: { GENERATE: 'generating' },
        },
        expired: {
          on: { GENERATE: 'generating' },
        },
        failure: {
          on: { GENERATE: 'generating' },
        },
      },
    });
  }

  private formatError(error: unknown): string {
    if (typeof error === 'string') return error;
    if (error instanceof Error) return error.message;
    return 'Request failed';
  }

  generate(): void {
    if (this.flowState() === 'generating') return;
    this.addPatientActor.send({ type: 'GENERATE' });
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
    this.stopTimers();
    this.dialogRef.close();
  }

  private startCountdown(expiresAtMs: number | null): void {
    this.stopCountdown();

    const expiryMs = expiresAtMs ?? NaN;
    if (!Number.isFinite(expiryMs)) {
      this.expiresInSeconds.set(null);
      return;
    }

    // Run outside Angular but re-enter for updates.
    this.zone.runOutsideAngular(() => {
      this.countdownSub = interval(250)
        .pipe(startWith(0))
        .subscribe(() => {
          this.zone.run(() => {
            const remaining = Math.max(
              0,
              Math.ceil((expiryMs - Date.now()) / 1000),
            );
            this.expiresInSeconds.set(remaining);

            if (remaining === 0) {
              this.addPatientActor.send({ type: 'EXPIRED' });
              this.stopCountdown();
            }

            this.cdr.markForCheck();
          });
        });
    });
  }

  private stopCountdown(): void {
    if (!this.countdownSub) return;
    this.countdownSub.unsubscribe();
    this.countdownSub = null;
  }

  private stopLinkWatch(): void {
    if (!this.watchSub) return;
    this.watchSub.unsubscribe();
    this.watchSub = null;
  }

  private stopCloseTimer(): void {
    if (!this.closeSub) return;
    this.closeSub.unsubscribe();
    this.closeSub = null;
  }

  private stopTimers(): void {
    this.stopCountdown();
    this.stopLinkWatch();
    this.stopCloseTimer();
  }

  private async ensureBaselineApprovedCount(): Promise<number> {
    try {
      const resp = await this.patients.getPatientsResponse();
      this.baselineApprovedCount = resp.patients?.length ?? 0;
    } catch (e) {
      this.log.warn('Could not load baseline patients count', { error: e });
      this.baselineApprovedCount = 0;
    }
    return this.baselineApprovedCount ?? 0;
  }

  private async generatePairing(): Promise<AddPatientGenerated> {
    this.links.clearError();

    const baseline = await this.ensureBaselineApprovedCount();
    this.baselineApprovedCount = baseline;

    const resp = await this.links.createPairingCode();

    const qrText = (resp.qrText ?? resp.code ?? '').trim();
    const qrDataUrl = qrText
      ? await QRCode.toDataURL(qrText, { width: 240, margin: 1 })
      : null;

    const expiresAtMs = resp.expiresAt
      ? new Date(resp.expiresAt).getTime()
      : null;

    return { pairing: resp, qrDataUrl, expiresAtMs };
  }

  private startLinkWatch(): void {
    this.stopLinkWatch();
    if (this.baselineApprovedCount == null) return;

    // Poll the doctor patient list; redeeming a pairing code creates an approved link.
    this.zone.runOutsideAngular(() => {
      this.watchSub = interval(1000)
        .pipe(
          startWith(0),
          exhaustMap(() => from(this.checkForApprovedIncrease())),
        )
        .subscribe();
    });
  }

  private async checkForApprovedIncrease(): Promise<void> {
    if (this.flowState() !== 'waiting') return;

    const baseline = this.baselineApprovedCount;
    if (baseline == null) return;

    try {
      const resp = await this.patients.getPatientsResponse();
      const approvedCount = resp.patients?.length ?? 0;
      if (approvedCount > baseline) {
        this.zone.run(() => this.addPatientActor.send({ type: 'CONNECTED' }));
      }
    } catch (e) {
      // Non-fatal; keep polling until expiry/dismiss.
      this.log.warn('Polling patients failed', { error: e });
    }
  }

  private scheduleClose(): void {
    this.stopCloseTimer();
    this.zone.runOutsideAngular(() => {
      this.closeSub = timer(3000)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          this.zone.run(() => this.close());
        });
    });
  }
}
