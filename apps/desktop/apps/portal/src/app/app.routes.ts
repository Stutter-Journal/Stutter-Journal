import { Route } from '@angular/router';
import { SectionPageComponent } from './pages/section-page.component';
import { AppShellComponent } from './shell/app-shell.component';

export const appRoutes: Route[] = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'patients',
      },
      {
        path: 'patients',
        component: SectionPageComponent,
        data: { title: 'Patients' },
      },
      {
        path: 'analytics',
        component: SectionPageComponent,
        data: { title: 'Analytics' },
      },
      {
        path: 'onboarding',
        component: SectionPageComponent,
        data: { title: 'Onboarding' },
      },
    ],
  },
];
