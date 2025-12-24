import { ApiError } from '@org/util';
import type { AuthClient } from './auth-client';

export type MockAuthMode = 'success' | 'invalid-credentials' | 'existing-user';

const baseDoctor = {
  id: 'doctor_01',
  displayName: 'Dr. Avery Shaw',
  role: 'Owner' as const,
  practiceId: null,
};

export const createMockAuthClient = (mode: MockAuthMode): AuthClient => ({
  async login({ email }) {
    if (mode === 'invalid-credentials') {
      throw new ApiError(
        401,
        { error: 'Invalid email or password' },
        'Unauthorized',
      );
    }

    return {
      doctor: {
        ...baseDoctor,
        email: email ?? 'care@eloquia.com',
      },
    };
  },

  async register({ email, displayName }) {
    if (mode === 'existing-user') {
      throw new ApiError(
        409,
        { error: 'User already exists. Use another email.' },
        'Conflict',
      );
    }

    return {
      doctor: {
        ...baseDoctor,
        id: 'doctor_02',
        email: email ?? 'care@eloquia.com',
        displayName: displayName ?? baseDoctor.displayName,
      },
    };
  },
});
