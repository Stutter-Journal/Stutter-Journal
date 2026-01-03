import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';
import {
  GetPatientsIdEntriesParams,
  ServerEntriesResponse,
  ServerEntryDTO,
} from '@org/contracts';

@Injectable({ providedIn: 'root' })
export class EntriesClientService {
  private readonly http = inject(HttpClient);

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async getEntries(
    patientId: string,
    filters?: GetPatientsIdEntriesParams,
  ): Promise<ServerEntryDTO[]> {
    const params = new HttpParams({
      fromObject: {
        from: filters?.from ?? '',
        to: filters?.to ?? '',
      },
    });

    const response = await this.execute(() =>
      this.http.get<ServerEntriesResponse>(`/patients/${patientId}/entries`, {
        params,
        withCredentials: true,
      }),
    );
    return response.entries ?? [];
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
