import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit, inject, Input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonComponent, FormFieldComponent } from '@org/portal-ui';
import { PracticeApi } from '@org/practice/data-access';
import { ApiError } from '@org/util';
import { DoctorContextService } from '../doctor/doctor-context.service';

interface ApiErrorPayload {
  message?: string;
  error?: string;
}

@Component({
  selector: 'portal-onboarding',
  standalone: true,
  imports: [ButtonComponent, FormFieldComponent, ReactiveFormsModule],
  templateUrl: './onboarding.component.html',
  styleUrl: './onboarding.component.scss',
})
export class OnboardingComponent implements OnInit {
  @Input() autoStartTour = false;
  @Input() initialTourStep = 0;

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly practiceApi = inject(PracticeApi);
  private readonly doctorContext = inject(DoctorContextService);

  readonly nameId = 'onboarding-practice-name';
  readonly addressId = 'onboarding-practice-address';
  readonly logoId = 'onboarding-practice-logo';

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    address: [''],
    logoUrl: [''],
  });

  loading = false;
  formError = '';
  tourVisible = false;
  tourStep = 0;

  readonly tourSteps = [
    {
      title: 'Invite your care team',
      body: 'Send secure invites to clinicians and coordinators who collaborate on patients.',
    },
    {
      title: 'Connect your patients',
      body: 'Import active patients or start with pending link requests to keep context tidy.',
    },
    {
      title: 'Review entries together',
      body: 'Notes, assessments, and plans stay in one place with gentle highlights for updates.',
    },
  ];

  get nameError(): string {
    const control = this.form.controls.name;
    if (!control.touched) {
      return '';
    }
    if (control.hasError('required')) {
      return 'Practice name is required.';
    }
    return '';
  }

  get nameDescribedBy(): string | null {
    const ids = ['onboarding-practice-name-hint'];
    if (this.nameError) {
      ids.push('onboarding-practice-name-error');
    }
    return ids.join(' ');
  }

  get addressDescribedBy(): string | null {
    return 'onboarding-practice-address-hint';
  }

  get logoDescribedBy(): string | null {
    return 'onboarding-practice-logo-hint';
  }

  ngOnInit(): void {
    if (this.autoStartTour) {
      this.startTour(this.initialTourStep);
    }
  }

  async onSubmit(): Promise<void> {
    this.formError = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    try {
      const payload = this.form.getRawValue();
      const response = await this.practiceApi.createPractice(payload);

      if (response.doctor) {
        this.doctorContext.setDoctor(response.doctor);
      }
      if (response.practice) {
        this.doctorContext.setPractice(response.practice);
      }

      this.startTour(0);
    } catch (error) {
      this.formError = this.mapError(error);
    } finally {
      this.loading = false;
    }
  }

  startTour(step: number): void {
    const safeStep = Math.min(
      Math.max(step, 0),
      Math.max(0, this.tourSteps.length - 1),
    );
    this.tourStep = safeStep;
    this.tourVisible = true;
  }

  nextTour(): void {
    if (this.tourStep >= this.tourSteps.length - 1) {
      this.finishOnboarding();
      return;
    }
    this.tourStep += 1;
  }

  skipTour(): void {
    this.tourVisible = false;
    void this.finishOnboarding();
  }

  finishOnboarding(): Promise<boolean> {
    this.tourVisible = false;
    return this.router.navigate(['/patients']);
  }

  private mapError(error: unknown): string {
    if (error instanceof ApiError) {
      if (typeof error.payload === 'string' && error.payload) {
        return error.payload;
      }
      const payload = error.payload as ApiErrorPayload | undefined;
      const message = payload?.message ?? payload?.error;
      if (message) {
        return message;
      }
      if (error.status === 409) {
        return 'A practice with that name already exists.';
      }
      if (error.status === 400) {
        return 'Check the practice details and try again.';
      }
    }

    if (error instanceof Error && error.message) {
      return error.message;
    }

    return 'We could not create the practice. Please try again.';
  }
}
