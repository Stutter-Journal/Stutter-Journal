import { InjectionToken, Provider } from '@angular/core';
import { ApiClient } from '@org/util';
import {
  DoctorLoginRequestDto,
  DoctorRegisterRequestDto,
  DoctorResponseDto,
} from '@org/models';

export interface AuthClient {
  login(payload: DoctorLoginRequestDto): Promise<DoctorResponseDto>;
  register(payload: DoctorRegisterRequestDto): Promise<DoctorResponseDto>;
}

class HttpAuthClient implements AuthClient {
  private readonly client = new ApiClient();

  login(payload: DoctorLoginRequestDto): Promise<DoctorResponseDto> {
    return this.client.post<DoctorResponseDto>('/doctor/login', payload);
  }

  register(payload: DoctorRegisterRequestDto): Promise<DoctorResponseDto> {
    return this.client.post<DoctorResponseDto>('/doctor/register', payload);
  }
}

export const AUTH_CLIENT = new InjectionToken<AuthClient>('AUTH_CLIENT');

export const provideAuthClient = (): Provider => ({
  provide: AUTH_CLIENT,
  useFactory: () => new HttpAuthClient(),
});
