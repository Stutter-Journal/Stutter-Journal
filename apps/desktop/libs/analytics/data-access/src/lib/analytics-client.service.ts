import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';

export interface AnalyticsRange {
  from: string;
  to: string;
}

export interface AnalyticsResponse {
  patientId: string;
  metrics: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsClientService {
  private readonly http = inject(HttpClient);

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async getAnalytics(patientId: string, range: AnalyticsRange): Promise<AnalyticsResponse> {
    const params = new HttpParams({ fromObject: { patientId, from: range.from, to: range.to } });
    return this.execute(() =>
      this.http.get<AnalyticsResponse>('/bff/analytics', {
        params,
        withCredentials: true,
      })
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
