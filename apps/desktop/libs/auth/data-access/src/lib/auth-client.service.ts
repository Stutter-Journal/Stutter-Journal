import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ErrorResponse, normalizeError } from '@org/util';
import {
  ServerDoctorLoginRequest,
  ServerDoctorRegisterRequest,
  ServerDoctorResponse,
} from '@org/contracts';

function isErrorResponse(value: unknown): value is ErrorResponse {
  return Boolean(
    value &&
      typeof value === 'object' &&
      'status' in value &&
      'message' in value,
  );
}

@Injectable({ providedIn: 'root' })
export class AuthClientService {
  private readonly http = inject(HttpClient);

  private readonly userSig = signal<ServerDoctorResponse | null>(null);
  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly user = computed(() => this.userSig());
  readonly isAuthed = computed(() => !!this.userSig());
  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  clearError(): void {
    this.errorSig.set(null);
  }

  async login(
    payload: ServerDoctorLoginRequest,
  ): Promise<ServerDoctorResponse> {
    const user = await this.execute(() =>
      this.http.post<ServerDoctorResponse>('/doctor/login', payload, {
        withCredentials: true,
      }),
    );
    this.userSig.set(user);
    return user;
  }

  async register(
    payload: ServerDoctorRegisterRequest,
  ): Promise<ServerDoctorResponse> {
    const user = await this.execute(() =>
      this.http.post<ServerDoctorResponse>('/doctor/register', payload, {
        withCredentials: true,
      }),
    );
    this.userSig.set(user);
    return user;
  }

  async me(): Promise<ServerDoctorResponse | null> {
    try {
      const user = await this.execute(() =>
        this.http.get<ServerDoctorResponse>('/doctor/me', {
          withCredentials: true,
        }),
      );
      this.userSig.set(user);
      return user;
    } catch (err) {
      if (isErrorResponse(err) && err.status === 401) {
        this.userSig.set(null);
        return null;
      }
      throw err;
    }
  }

  async logout(): Promise<void> {
    await this.execute(() =>
      this.http.post<void>('/doctor/logout', {}, { withCredentials: true }),
    );
    this.userSig.set(null);
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
