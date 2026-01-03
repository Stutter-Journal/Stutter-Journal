import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';

export interface PatientSummary {
  id: string;
  name: string;
  email?: string;
  createdAt?: string;
}

export interface PatientFilters {
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class PatientsClientService {
  private readonly http = inject(HttpClient);

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async getPatients(filters?: PatientFilters): Promise<PatientSummary[]> {
    const params = new HttpParams({ fromObject: { search: filters?.search ?? '' } });
    return this.execute(() =>
      this.http.get<PatientSummary[]>('/bff/patients', {
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
