import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { ErrorResponse } from '@org/util';
import {
  execute,
  GetPatientsIdEntriesParams,
  ServerEntriesResponse,
  ServerEntryDTO,
  ServerPatientDTO,
} from '@org/contracts';

export interface RecentEntriesRow {
  entry?: ServerEntryDTO;
  patient?: ServerPatientDTO;
}

export interface RecentEntriesResponse {
  rows?: RecentEntriesRow[];
}

@Injectable({ providedIn: 'root' })
export class EntriesClientService {
  private readonly baseUrl = '/api';
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

    const response = await execute(() =>
      this.http.get<ServerEntriesResponse>(`${this.baseUrl}/patients/${patientId}/entries`, {
        params,
        withCredentials: true,
      }), this.loadingSig, this.errorSig
    );
    return response.entries ?? [];
  }

  async getRecentEntries(limit = 5): Promise<RecentEntriesResponse> {
    const query = `?limit=${encodeURIComponent(String(limit))}`;

    return await execute(
      () =>
        this.http.get<RecentEntriesResponse>(`${this.baseUrl}/entries/recent${query}`, {
          withCredentials: true,
        }),
      this.loadingSig,
      this.errorSig,
    );
  }
}
