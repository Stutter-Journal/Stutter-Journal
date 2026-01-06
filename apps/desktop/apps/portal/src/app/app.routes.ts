/* eslint-disable */
import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: 'landing',
    loadComponent: () => import('@org/feat-landing').then((m) => m.FeatLanding),
  },
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
];
