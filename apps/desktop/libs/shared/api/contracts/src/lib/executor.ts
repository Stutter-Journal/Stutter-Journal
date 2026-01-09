import { firstValueFrom, Observable } from 'rxjs';
import { WritableSignal } from '@angular/core';

export interface ErrorResponse {
  status?: number;
  code?: string;
  message: string;
  details?: unknown;
}

// Lightweight normalizer to keep executor self-contained and avoid cross-lib deps
function normalizeError(err: unknown): ErrorResponse {
  const anyErr = err as any;
  const status = typeof anyErr?.status === 'number' ? anyErr.status : 0;

  const message =
    typeof anyErr?.message === 'string'
      ? anyErr.message
      : typeof anyErr === 'string'
        ? anyErr
        : 'Request failed';

  return {
    status,
    code: typeof anyErr?.code === 'string' ? anyErr.code : undefined,
    message,
    details: anyErr?.error ?? anyErr,
  };
}

export async function execute<T>(
  request: () => Observable<T>,
  loadingSig: WritableSignal<boolean>,
  errorSig: WritableSignal<ErrorResponse | null>,
): Promise<T> {
  loadingSig.set(true);
  errorSig.set(null);

  try {
    return await firstValueFrom(request());
  } catch (err) {
    const normalized = normalizeError(err);
    errorSig.set(normalized);

    throw normalized;
  } finally {
    loadingSig.set(false);
  }
}
