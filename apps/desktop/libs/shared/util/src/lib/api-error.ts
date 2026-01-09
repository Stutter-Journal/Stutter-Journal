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
    const body = err.error as
      | { message?: string; error?: string; code?: string; details?: unknown }
      | undefined;
    return {
      status: err.status,
      code: body?.code,
      message: body?.message ?? body?.error ?? err.message ?? 'Request failed',
      details: body?.details ?? err.error,
    };
  }

  if (err instanceof Error) {
    return { status: 0, message: err.message, details: err };
  }

  return { status: 0, message: 'Unknown error', details: err };
}
