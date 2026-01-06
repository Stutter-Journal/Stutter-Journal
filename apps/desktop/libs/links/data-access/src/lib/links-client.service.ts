import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';
import {
  ServerLinkApproveResponse,
  ServerLinkInviteRequest,
  ServerLinkInviteRequestBody,
  ServerLinkResponse,
} from '@org/contracts';

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

  async invitePatient(
    payload: ServerLinkInviteRequest,
  ): Promise<ServerLinkResponse> {
    return this.execute(() =>
      this.http.post<ServerLinkResponse>('/links/invite', payload, {
        withCredentials: true,
      }),
    );
  }

  async requestLink(
    payload: ServerLinkInviteRequestBody,
  ): Promise<ServerLinkResponse> {
    return this.execute(() =>
      this.http.post<ServerLinkResponse>('/links/request', payload, {
        withCredentials: true,
      }),
    );
  }

  async approveLink(linkId: string): Promise<ServerLinkApproveResponse> {
    return this.execute(() =>
      this.http.post<ServerLinkApproveResponse>(
        `/links/${linkId}/approve`,
        {},
        { withCredentials: true },
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
