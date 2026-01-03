import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';
import {
  ServerPracticeCreateRequest,
  ServerPracticeCreateResponse,
  ServerPracticeResponse,
} from '@org/contracts';

@Injectable({ providedIn: 'root' })
export class PracticeClientService {
  private readonly http = inject(HttpClient);

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async getPractice(): Promise<ServerPracticeResponse> {
    return this.execute(() =>
      this.http.get<ServerPracticeResponse>('/practice', { withCredentials: true })
    );
  }

  async setupPractice(payload: ServerPracticeCreateRequest): Promise<ServerPracticeCreateResponse> {
    return this.execute(() =>
      this.http.post<ServerPracticeCreateResponse>('/practice', payload, { withCredentials: true })
    );
  }

  private async execute<T>(request: () => Observable<T>): Promise<T> {
    this.loadingSig.set(true);
    this.errorSig.set(null);
    try {
      return await firstValueFrom(request());
    } catch (err) {
      const normalized = normalizeError(err);
      this.errorSig.set(normalized);
      throw normalized;
    } finally {
      this.loadingSig.set(false);
    }
  }
}
