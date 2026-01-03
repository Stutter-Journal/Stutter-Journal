import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';
import {
  GetPatientsIdAnalyticsParams,
  ServerAnalyticsResponse,
} from '@org/contracts';

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

  async getAnalytics(
    patientId: string,
    params?: GetPatientsIdAnalyticsParams,
  ): Promise<ServerAnalyticsResponse> {
    const httpParams = new HttpParams({
      fromObject: { range: params?.range ?? '7' },
    });
    return this.execute(() =>
      this.http.get<ServerAnalyticsResponse>(
        `/patients/${patientId}/analytics`,
        {
          params: httpParams,
          withCredentials: true,
        },
      ),
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
