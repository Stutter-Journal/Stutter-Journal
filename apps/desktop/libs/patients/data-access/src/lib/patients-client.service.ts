import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { ErrorResponse } from '@org/util';
import { execute, ServerPatientsResponse } from '@org/contracts';

@Injectable({ providedIn: 'root' })
export class PatientsClientService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  async getPatientsResponse(): Promise<ServerPatientsResponse> {
    return await execute(
      () =>
        this.http.get<ServerPatientsResponse>(`${this.base}/patients`, {
          withCredentials: true,
        }),
      this.loadingSig,
      this.errorSig,
    );
  }
}
