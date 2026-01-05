/* eslint-disable */
import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: 'landing',
    loadComponent: () => import('@org/feat-cascade').then((m) => m.FeatCascade),
  },
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
];
