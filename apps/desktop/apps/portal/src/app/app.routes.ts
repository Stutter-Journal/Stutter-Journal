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
    children: [
      {
        path: 'entries',
        loadComponent: () =>
          import('@org/feat-entry-list').then((m) => m.FeatEntryList),
      },
      {
        path: 'patients',
        loadComponent: () =>
          import('@org/feat-patients-overview').then(
            (m) => m.FeatPatientsOverview,
          ),
      },
      // TODO: This is for later
      // {
      //   path: 'patients/new',
      //   loadComponent: () =>
      //     import('@org/feat-patients').then((m) => m.PatientCreatePage),
      // },
      // {
      //   path: 'patients/:id',
      //   loadComponent: () =>
      //     import('@org/feat-patients').then((m) => m.PatientDetailPage),
      // },
    ],
  },
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full',
  },
  { path: '**', redirectTo: 'landing' },
];
