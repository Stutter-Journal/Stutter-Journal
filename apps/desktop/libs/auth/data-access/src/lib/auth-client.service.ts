import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { ErrorResponse } from '@org/util';
import {
  execute,
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
  private readonly base = 'api';

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
    const user = await execute(
      () =>
        this.http.post<ServerDoctorResponse>(
          `${this.base}/doctor/login`,
          payload,
          {
            withCredentials: true,
          },
        ),
      this.loadingSig,
      this.errorSig,
    );
    const doctor = this.unwrapDoctor(user);
    this.userSig.set(doctor);
    return doctor;
  }

  async register(
    payload: ServerDoctorRegisterRequest,
  ): Promise<ServerDoctorResponse> {
    const user = await execute(
      () =>
        this.http.post<ServerDoctorResponse>(
          `${this.base}/doctor/register`,
          payload,
          {
            withCredentials: true,
          },
        ),
      this.loadingSig,
      this.errorSig,
    );

    const doctor = this.unwrapDoctor(user);

    this.userSig.set(doctor);

    return doctor;
  }

  async me(): Promise<ServerDoctorResponse | null> {
    try {
      const user = await execute(
        () =>
          this.http.get<ServerDoctorResponse>(`${this.base}/doctor/me`, {
            withCredentials: true,
          }),
        this.loadingSig,
        this.errorSig,
      );
      const doctor = this.unwrapDoctor(user);
      this.userSig.set(doctor);
      return doctor;
    } catch (err) {
      if (isErrorResponse(err) && err.status === 401) {
        this.userSig.set(null);
        return null;
      }
      throw err;
    }
  }

  async logout(): Promise<void> {
    await execute(
      () =>
        this.http.post<void>(
          `${this.base}/doctor/logout`,
          {},
          { withCredentials: true },
        ),
      this.loadingSig,
      this.errorSig,
    );

    this.userSig.set(null);
  }

  // TODO: Refactor this
  private unwrapDoctor(payload: unknown): ServerDoctorResponse {
    const anyPayload = payload as any;

    // Common response envelopes we see:
    // - { data: { doctor: {...} } }
    // - { data: {...doctor fields...} }
    // - { doctor: {...} }
    // - { ...doctor fields }
    if (anyPayload?.data?.doctor) return anyPayload.data.doctor;
    if (anyPayload?.doctor) return anyPayload.doctor;
    if (anyPayload?.data) return anyPayload.data;
    return anyPayload as ServerDoctorResponse;
  }
}
