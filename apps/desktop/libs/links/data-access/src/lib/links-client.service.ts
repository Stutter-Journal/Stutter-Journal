import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { ErrorResponse } from '@org/util';
import {
  execute,
  ServerPairingCodeCreateResponse,
  ServerPairingCodeRedeemRequest,
  ServerLinkApproveResponse,
  ServerLinkInviteRequest,
  ServerLinkInviteRequestBody,
  ServerLinkResponse,
} from '@org/contracts';

@Injectable({ providedIn: 'root' })
export class LinksClientService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

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
    return await execute(
      () =>
        this.http.post<ServerLinkResponse>(
          `${this.base}/links/invite`,
          payload,
          {
            withCredentials: true,
          },
        ),
      this.loadingSig,
      this.errorSig,
    );
  }

  async requestLink(
    payload: ServerLinkInviteRequestBody,
  ): Promise<ServerLinkResponse> {
    return await execute(
      () =>
        this.http.post<ServerLinkResponse>(
          `${this.base}/links/request`,
          payload,
          {
            withCredentials: true,
          },
        ),
      this.loadingSig,
      this.errorSig,
    );
  }

  async approveLink(linkId: string): Promise<ServerLinkApproveResponse> {
    return await execute(
      () =>
        this.http.post<ServerLinkApproveResponse>(
          `${this.base}/links/${linkId}/approve`,
          {},
          { withCredentials: true },
        ),
      this.loadingSig,
      this.errorSig,
    );
  }

  async createPairingCode(): Promise<ServerPairingCodeCreateResponse> {
    return await execute(
      () =>
        this.http.post<ServerPairingCodeCreateResponse>(
          `${this.base}/links/pairing-code`,
          {},
          { withCredentials: true },
        ),
      this.loadingSig,
      this.errorSig,
    );
  }

  async redeemPairingCode(
    payload: ServerPairingCodeRedeemRequest,
  ): Promise<ServerLinkResponse> {
    return await execute(
      () =>
        this.http.post<ServerLinkResponse>(
          `${this.base}/links/pairing-code/redeem`,
          payload,
          { withCredentials: true },
        ),
      this.loadingSig,
      this.errorSig,
    );
  }
}
