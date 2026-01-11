/* eslint-disable */
import { Route } from '@angular/router';
import { onboardingGuard } from '@org/auth-data-access';

export const appRoutes: Route[] = [
  {
    path: 'landing',
    loadComponent: () => import('@org/feat-landing').then((m) => m.FeatLanding),
  },
  {
    path: 'app',
    canActivate: [onboardingGuard],
    loadComponent: () => import('@org/dashboard').then((m) => m.Dashboard),
  },
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: 'landing' },
];
