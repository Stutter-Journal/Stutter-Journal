import { ErrorResponse, normalizeError } from '@org/util';
import { firstValueFrom, Observable } from 'rxjs';
import { WritableSignal } from '@angular/core';

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
