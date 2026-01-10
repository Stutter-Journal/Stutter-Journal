import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthClientService } from './auth-client.service';
import { LoggerService } from '@org/util';

// TODO: I believe this guard contains too much unnecessary complexity.
export const onboardingGuard: CanActivateFn = async () => {
  const auth = inject(AuthClientService);
  const router = inject(Router);
  const log = inject(LoggerService);

  log.debug('Onboarding guard invoked');

  // Prefer cached user; otherwise hydrate once via cookie.
  const cachedUser = auth.user();
  const user = cachedUser ?? (await auth.me());

  log.debug('User for guard decision', {
    cached: !!cachedUser,
    id: user?.id,
    practiceId: user?.practiceId,
  });

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
