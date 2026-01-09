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
  const anyErr = err as any;
  const status = typeof anyErr?.status === 'number' ? anyErr.status : 0;

  const upstreamPayload = anyErr?.error;

  let payloadMessage: string | undefined;
  let payloadCode: string | undefined;
  let payloadDetails: unknown | undefined;

  // Prefer structured backend errors when available.
  const parsed = serverErrorResponseSchema.safeParse(upstreamPayload);
  if (parsed.success) {
    payloadMessage = parsed.data.error ?? parsed.data.message;
    payloadCode = parsed.data.code;
    payloadDetails = parsed.data.details;
  } else if (typeof upstreamPayload === 'string') {
    payloadMessage = upstreamPayload;
  } else if (upstreamPayload && typeof upstreamPayload === 'object') {
    const anyPayload = upstreamPayload as any;
    payloadMessage =
      (typeof anyPayload.error === 'string' ? anyPayload.error : undefined) ??
      (typeof anyPayload.message === 'string' ? anyPayload.message : undefined);
    payloadCode = typeof anyPayload.code === 'string' ? anyPayload.code : undefined;
    payloadDetails = anyPayload.details;
  }

  const rawMessage =
    typeof anyErr?.message === 'string'
      ? anyErr.message
      : typeof anyErr === 'string'
        ? anyErr
        : 'Request failed';

  // Angular's HttpErrorResponse.message is usually "Http failure response for ..."; don't show that if we have a better backend message.
  const isAngularGeneric =
    typeof rawMessage === 'string' && rawMessage.startsWith('Http failure response for ');

  const message = payloadMessage ?? (isAngularGeneric ? 'Request failed' : rawMessage);

  return {
    status,
    code: payloadCode ?? (typeof anyErr?.code === 'string' ? anyErr.code : undefined),
    message,
    details: payloadDetails ?? upstreamPayload ?? anyErr,
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
