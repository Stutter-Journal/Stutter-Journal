/* eslint-disable */
import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: 'landing',
    loadComponent: () =>
      import('@org/feat-register').then((m) => m.FeatRegister),
  },
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
];
