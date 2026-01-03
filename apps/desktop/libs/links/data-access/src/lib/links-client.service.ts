import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';

export interface InvitePatientRequest {
  email: string;
  name?: string;
  patientId?: string;
}

export interface InvitePatientResponse {
  linkToken: string;
  expiresAt?: string;
}

export interface ApproveLinkRequest {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class LinksClientService {
  private readonly http = inject(HttpClient);

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async invitePatient(payload: InvitePatientRequest): Promise<InvitePatientResponse> {
    return this.execute(() =>
      this.http.post<InvitePatientResponse>('/bff/links/invite', payload, {
        withCredentials: true,
      })
    );
  }

  async approveLink(request: ApproveLinkRequest): Promise<void> {
    await this.execute(() =>
      this.http.post<void>('/bff/links/approve', request, { withCredentials: true })
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
