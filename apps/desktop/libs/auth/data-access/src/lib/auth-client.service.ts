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
  private readonly base = '/api';

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
    const get = (value: unknown, key: string): unknown => {
      if (!value || typeof value !== 'object') return undefined;
      return (value as Record<string, unknown>)[key];
    };

    const asString = (value: unknown): string | undefined =>
      typeof value === 'string' ? value : undefined;

    // Common response envelopes we see:
    // - { data: { doctor: {...} } }
    // - { data: {...doctor fields...} }
    // - { doctor: {...} }
    // - { ...doctor fields }
    const doctorLike =
      get(get(payload, 'data'), 'doctor') ??
      get(payload, 'doctor') ??
      get(payload, 'data') ??
      payload;

    if (!doctorLike || typeof doctorLike !== 'object') {
      return {};
    }

    // Be tolerant to upstream casing differences (snake_case vs camelCase)
    // since the BFF is a proxy and upstream responses can vary.
    return {
      id: asString(get(doctorLike, 'id'))
        ?? asString(get(doctorLike, 'doctorId'))
        ?? asString(get(doctorLike, 'doctor_id')),
      practiceId: asString(get(doctorLike, 'practiceId'))
        ?? asString(get(doctorLike, 'practice_id')),
      displayName: asString(get(doctorLike, 'displayName'))
        ?? asString(get(doctorLike, 'display_name')),
      email: asString(get(doctorLike, 'email')),
      role: asString(get(doctorLike, 'role')),
    };
  }
}
