import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { normalizeError } from '@org/util';

/**
 * Ensures cookies are sent to the BFF and normalizes error payloads.
 */
export const bffAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const withCreds = req.clone({ withCredentials: true });
  return next(withCreds).pipe(
    catchError((err) => throwError(() => normalizeError(err))),
  );
};
