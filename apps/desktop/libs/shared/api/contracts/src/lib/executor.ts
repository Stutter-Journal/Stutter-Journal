import { firstValueFrom, Observable } from 'rxjs';
import { WritableSignal } from '@angular/core';
import { serverErrorResponseSchema } from './schemas';

export interface ErrorResponse {
  status: number;
  code?: string;
  message: string;
  details?: unknown;
}

// Lightweight normalizer to keep executor self-contained and avoid cross-lib deps
function normalizeError(err: unknown): ErrorResponse {
  const e = err as {
    status?: unknown;
    message?: unknown;
    error?: unknown;
    code?: unknown;
  };

  const status = typeof e?.status === 'number' ? e.status : 0;
  const payload = e?.error;

  const backend = serverErrorResponseSchema.safeParse(payload);
  const backendMessage = backend.success ? backend.data.error : undefined;

  const rawMessage =
    typeof e?.message === 'string'
      ? e.message
      : typeof err === 'string'
        ? err
        : 'Request failed';

  const isAngularGeneric = rawMessage.startsWith('Http failure response for ');
  const message = backendMessage ?? (isAngularGeneric ? 'Request failed' : rawMessage);

  return {
    status,
    code: typeof e?.code === 'string' ? e.code : undefined,
    message,
    details: payload ?? err,
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
