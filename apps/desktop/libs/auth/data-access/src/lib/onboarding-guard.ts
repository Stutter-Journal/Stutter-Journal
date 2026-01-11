import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthClientService } from '@org/auth-data-access';
import { LoggerService } from '@org/util';

// TODO: I believe this guard contains too much unnecessary complexity.
export const onboardingGuard: CanActivateFn = async () => {
  const auth = inject(AuthClientService);
  const router = inject(Router);
  const log = inject(LoggerService);

  log.debug('Onboarding guard invoked');

  // If we already know the user, avoid the network call.
  let user = auth.user();

  log.debug('Cached user before hydrate', {
    id: user?.id,
    practiceId: user?.practiceId,
  });

  // If user isn't loaded yet, try to hydrate via cookie
  if (!user) {
    user = await auth.me();
    log.debug('User hydrated via me()', {
      id: user?.id,
      practiceId: user?.practiceId,
    });
  }

  // If the cached user was missing practiceId, refresh from the server to
  // catch newly-created practices before redirecting.
  if (user && !user.practiceId) {
    log.debug('User missing practiceId, refreshing');
    user = await auth.me();
    log.debug('User after refresh', {
      id: user?.id,
      practiceId: user?.practiceId,
    });
  }

  log.info(`User with username ${user?.displayName} has been fetched!`);

  // Not logged in -> go to landing
  if (!user) {
    log.warn('Guard redirect: unauthenticated user');
    return router.parseUrl('/landing');
  }

  // Logged in but not onboarded -> go to landing (it will show onboarding)
  if (!user.practiceId) {
    log.info('Guard redirect: user missing practiceId');
    return router.parseUrl('/landing');
  }

  // Onboarded -> allow route
  log.debug('Guard passed: user onboarded');
  return true;
};
