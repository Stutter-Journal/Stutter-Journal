import { Component, inject, OnInit, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FeatCascade } from '@org/feat-cascade';
import { AuthClientService } from '@org/auth-data-access';
import { Router } from '@angular/router';
import { FeatPracticeSetup } from '@org/feat-practice-setup';
import { LoggerService } from '@org/util';

type UiState = 'checking' | 'auth' | 'onboarding';

@Component({
  selector: 'lib-feat-landing',
  imports: [NgOptimizedImage, FeatCascade, FeatPracticeSetup],
  templateUrl: './feat-landing.html',
  styleUrl: './feat-landing.css',
})
export class FeatLanding implements OnInit {
  private readonly auth = inject(AuthClientService);
  private readonly router = inject(Router);
  private readonly log = inject(LoggerService);

  readonly state = signal<UiState>('checking');

  async ngOnInit(): Promise<void> {
    this.log.info('Landing init: checking user state');
    await this.recomputeState();
  }

  async recomputeState(): Promise<void> {
    // hydrate user from cookie if present
    this.log.debug('Recomputing landing UI state');
    await this.auth.me();

    const user = this.auth.user();

    this.log.debug('User after hydrate', {
      id: user?.id,
      practiceId: user?.practiceId,
    });

    if (!user) {
      this.state.set('auth');
      this.log.info('User not found, showing auth');
      return;
    }

    if (!user.practiceId) {
      this.state.set('onboarding');
      this.log.info('User needs onboarding');
      return;
    }

    // already onboarded
    this.log.info('User onboarded, navigating to app');
    await this.router.navigateByUrl('/app'); // change to your real post-auth route
  }

  async onBoardingCompleted() {
    // Make sure userSig reflects the new practiceId
    this.log.info('Onboarding completed, refreshing user');
    await this.auth.me();

    // Now go to dashboard
    this.log.info('Navigating to app after onboarding');
    await this.router.navigateByUrl('/app');
  }
}
