import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { ErrorResponse } from '@org/util';
import {
  execute,
  ServerPracticeCreateRequest,
  ServerPracticeCreateResponse,
  ServerPracticeResponse,
} from '@org/contracts';

@Injectable({ providedIn: 'root' })
export class PracticeClientService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async getPractice(): Promise<ServerPracticeResponse> {
    return await execute(
      () =>
        this.http.get<ServerPracticeResponse>(`${this.base}/practice`, {
          withCredentials: true,
        }),
      this.loadingSig,
      this.errorSig,
    );
  }

  async create(
    payload: ServerPracticeCreateRequest,
  ): Promise<ServerPracticeCreateResponse> {
    return await execute(
      () =>
        this.http.post<ServerPracticeCreateResponse>(
          `${this.base}/practice`,
          payload,
          { withCredentials: true },
        ),
      this.loadingSig,
      this.errorSig,
    );
  }
}
