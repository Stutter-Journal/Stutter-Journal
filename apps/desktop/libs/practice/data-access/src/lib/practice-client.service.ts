import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';

export interface PracticeInfo {
  id: string;
  name: string;
  timezone?: string;
}

export interface PracticeSetupRequest {
  name: string;
  timezone?: string;
}

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

  async getPractice(): Promise<PracticeInfo> {
    return this.execute(() =>
      this.http.get<PracticeInfo>('/bff/practice', { withCredentials: true })
    );
  }

  async setupPractice(payload: PracticeSetupRequest): Promise<PracticeInfo> {
    return this.execute(() =>
      this.http.post<PracticeInfo>('/bff/practice', payload, { withCredentials: true })
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
