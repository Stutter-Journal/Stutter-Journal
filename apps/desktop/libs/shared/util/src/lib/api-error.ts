import { HttpErrorResponse } from '@angular/common/http';

export interface ErrorResponse {
  status: number;
  code?: string;
  message: string;
  details?: unknown;
}

/**
 * Best-effort normalization for backend/BFF errors. Keeps the surface stable for UI.
 */
export function normalizeError(err: unknown): ErrorResponse {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as { error?: unknown; code?: unknown } | undefined;

    const backendError =
      body && typeof body === 'object' && typeof body.error === 'string'
        ? body.error
        : undefined;

    const rawMessage = typeof err.message === 'string' ? err.message : 'Request failed';
    const isAngularGeneric = rawMessage.startsWith('Http failure response for ');

    return {
      status: err.status,
      code: typeof body?.code === 'string' ? body.code : undefined,
      message: backendError ?? (isAngularGeneric ? 'Request failed' : rawMessage),
      details: err.error,
    };
  }

  if (err instanceof Error) {
    return { status: 0, message: err.message, details: err };
  }

  return { status: 0, message: 'Unknown error', details: err };
}
