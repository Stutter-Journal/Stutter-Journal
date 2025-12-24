import { Injectable, inject } from '@angular/core';
import { ApiError } from '@org/util';
import { AUTH_CLIENT, AuthClient } from './auth-client';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly client: AuthClient = inject(AUTH_CLIENT);

  async login(email: string, password: string): Promise<string | null> {
    try {
      await this.client.login({ email, password });
      return null;
    } catch (error) {
      return this.mapLoginError(error);
    }
  }

  async register(payload: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }): Promise<string | null> {
    try {
      const displayName = `${payload.firstName} ${payload.lastName}`.trim();
      await this.client.register({
        displayName,
        email: payload.email,
        password: payload.password,
      });
      return null;
    } catch (error) {
      return this.mapRegisterError(error);
    }
  }

  private mapLoginError(error: unknown): string {
    if (error instanceof ApiError) {
      if (error.status === 401) {
        return 'That email and password do not match our records.';
      }
      if (error.status === 400) {
        return (
          this.readApiErrorMessage(error.payload) ??
          'Check the form details and try again.'
        );
      }
      const message = this.readApiErrorMessage(error.payload);
      if (message) {
        return message;
      }
    }

    if (error instanceof Error && error.message) {
      return error.message;
    }

    return 'We could not sign you in. Please try again.';
  }

  private mapRegisterError(error: unknown): string {
    if (error instanceof ApiError) {
      if (error.status === 409) {
        return 'An account with that email already exists.';
      }
      if (error.status === 400) {
        return 'Check the form details and try again.';
      }
      const message = this.readApiErrorMessage(error.payload);
      if (message) {
        return message;
      }
    }

    return 'We could not create your account. Please try again.';
  }

  private readApiErrorMessage(payload: unknown): string | undefined {
    if (
      payload &&
      typeof payload === 'object' &&
      'error' in payload &&
      typeof (payload as { error: unknown }).error === 'string'
    ) {
      return (payload as { error: string }).error;
    }
    return undefined;
  }
}
