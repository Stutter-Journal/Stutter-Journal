import { Injectable, inject } from '@angular/core';
import { AUTH_CLIENT } from './auth-client';

interface AuthErrorPayload {
  status: number;
  statusText: string;
  message?: string;
  code?: string;
}

interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly client = inject(AUTH_CLIENT);

  async login(email: string, password: string): Promise<string | null> {
    try {
      const response = await this.client.signIn.email({ email, password });
      if (response.error) {
        return this.mapLoginError(response.error as AuthErrorPayload);
      }
      return null;
    } catch (error) {
      return this.mapUnexpectedError(error, 'login');
    }
  }

  async register(payload: RegisterPayload): Promise<string | null> {
    try {
      const name = `${payload.firstName} ${payload.lastName}`.trim();
      const response = await this.client.signUp.email({
        name,
        email: payload.email,
        password: payload.password,
      });
      if (response.error) {
        return this.mapRegisterError(response.error as AuthErrorPayload);
      }
      return null;
    } catch (error) {
      return this.mapUnexpectedError(error, 'register');
    }
  }

  private mapLoginError(error: AuthErrorPayload): string {
    const code = error.code?.toUpperCase();
    const known = {
      INVALID_EMAIL_OR_PASSWORD:
        'That email and password do not match our records.',
      INVALID_PASSWORD: 'That email and password do not match our records.',
      USER_EMAIL_NOT_FOUND: 'That email and password do not match our records.',
      EMAIL_NOT_VERIFIED: 'Please verify your email before signing in.',
    } as const;

    if (code && code in known) {
      return known[code as keyof typeof known];
    }

    if (error.status === 401) {
      return 'That email and password do not match our records.';
    }

    if (error.status === 403) {
      return 'Your account is pending approval.';
    }

    if (error.message) {
      return error.message;
    }

    return 'We could not sign you in. Please try again.';
  }

  private mapRegisterError(error: AuthErrorPayload): string {
    const code = error.code?.toUpperCase();
    const known = {
      USER_ALREADY_EXISTS: 'An account with that email already exists.',
      USER_ALREADY_EXISTS_USE_ANOTHER_EMAIL:
        'An account with that email already exists.',
      PASSWORD_TOO_SHORT: 'Use at least 8 characters.',
      PASSWORD_TOO_LONG: 'Use a shorter password.',
    } as const;

    if (code && code in known) {
      return known[code as keyof typeof known];
    }

    if (error.status === 409) {
      return 'An account with that email already exists.';
    }

    if (error.status === 400) {
      return 'Check the form details and try again.';
    }

    if (error.message) {
      return error.message;
    }

    return 'We could not create your account. Please try again.';
  }

  private mapUnexpectedError(
    error: unknown,
    mode: 'login' | 'register'
  ): string {
    if (error instanceof Error && error.message) {
      return error.message;
    }
    return mode === 'login'
      ? 'We could not sign you in. Please try again.'
      : 'We could not create your account. Please try again.';
  }
}
