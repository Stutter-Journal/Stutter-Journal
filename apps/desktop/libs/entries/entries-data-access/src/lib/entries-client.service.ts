import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';

export interface EntryFilters {
  from?: string;
  to?: string;
}

export interface Entry {
  id: string;
  patientId: string;
  createdAt: string;
  author?: string;
  summary?: string;
}

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

  async getEntries(patientId: string, filters?: EntryFilters): Promise<Entry[]> {
    const params = new HttpParams({
      fromObject: {
        patientId,
        from: filters?.from ?? '',
        to: filters?.to ?? '',
      },
    });

    return this.execute(() =>
      this.http.get<Entry[]>('/bff/entries', {
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
