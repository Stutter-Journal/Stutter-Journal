import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';
import {
  execute,
  ServerPatientDTO,
  ServerPatientsResponse,
} from '@org/contracts';

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

  async getPatients(filters?: PatientFilters): Promise<ServerPatientDTO[]> {
    const params = new HttpParams({
      fromObject: { search: filters?.search ?? '' },
    });

    const response = await execute(() =>
      this.http.get<ServerPatientsResponse>('/patients', {
        params,
        withCredentials: true,
      }), this.loadingSig, this.errorSig
    );

    return response.patients ?? [];
  }
}
