import { InjectionToken, Provider } from '@angular/core';
import { createAuthClient } from 'better-auth/client';

export type AuthClientInstance = ReturnType<typeof createAuthClient>;

export const AUTH_CLIENT = new InjectionToken<AuthClientInstance>(
  'AUTH_CLIENT'
);

export const provideAuthClient = (): Provider => ({
  provide: AUTH_CLIENT,
  useFactory: () =>
    createAuthClient({
      basePath: '/api/auth',
      fetchOptions: {
        throw: false,
      },
    }),
});
