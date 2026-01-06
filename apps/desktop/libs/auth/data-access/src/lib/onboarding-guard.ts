import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthClientService } from '@org/auth-data-access';
import {LoggerService} from '@org/util';

export const onboardingGuard: CanActivateFn = async () => {
  const auth = inject(AuthClientService);
  const router = inject(Router);
  const log = inject(LoggerService);

  // If we already know the user, avoid the network call.
  let user = auth.user();

  // If user isn't loaded yet, try to hydrate via cookie
  if (!user) {
    user = await auth.me();
  }

  log.info(`User with username ${user?.displayName} has been fetched!`);

  // Not logged in -> go to landing
  if (!user) return router.parseUrl('/landing');

  // Logged in but not onboarded -> go to landing (it will show onboarding)
  if (!user.practiceId) return router.parseUrl('/landing');

  // Onboarded -> allow route
  return true;
};
