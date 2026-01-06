import { Component, inject, OnInit, signal } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FeatCascade } from '@org/feat-cascade';
import { AuthClientService } from '@org/auth-data-access';
import { Router } from '@angular/router';
import { FeatPracticeSetup } from '@org/feat-practice-setup';

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

  readonly state = signal<UiState>('checking');

  async ngOnInit(): Promise<void> {
    await this.recomputeState();
  }

  async recomputeState(): Promise<void> {
    // hydrate user from cookie if present
    await this.auth.me();

    const user = this.auth.user();

    if (!user) {
      this.state.set('auth');
      return;
    }

    if (!user.practiceId) {
      this.state.set('onboarding');
      return;
    }

    // already onboarded
    await this.router.navigateByUrl('/'); // change to your real post-auth route
  }
}
