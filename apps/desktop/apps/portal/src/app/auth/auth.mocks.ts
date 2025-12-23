import { createAuthClient } from 'better-auth/client';
import type { AuthClientInstance } from './auth-client';

export type MockAuthMode = 'success' | 'invalid-credentials' | 'existing-user';

const jsonResponse = (body: unknown, status = 200, statusText?: string) =>
  new Response(JSON.stringify(body), {
    status,
    statusText,
    headers: {
      'Content-Type': 'application/json',
    },
  });

const readUrl = (input: RequestInfo | URL): string => {
  if (typeof input === 'string') {
    return input;
  }
  if (input instanceof URL) {
    return input.toString();
  }
  return input.url;
};

const parseJsonBody = async (body?: BodyInit | null): Promise<any> => {
  if (!body || typeof body !== 'string') {
    return {};
  }
  try {
    return JSON.parse(body);
  } catch {
    return {};
  }
};

const createMockAuthFetch = (mode: MockAuthMode) => {
  return async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
    const url = readUrl(input);
    const method = (init?.method ?? 'GET').toUpperCase();
    const body = await parseJsonBody(init?.body ?? null);

    if (method === 'POST' && url.includes('/sign-in/email')) {
      if (mode === 'invalid-credentials') {
        return jsonResponse(
          {
            message: 'Invalid email or password',
            code: 'INVALID_EMAIL_OR_PASSWORD',
          },
          401,
          'Unauthorized'
        );
      }

      return jsonResponse({
        redirect: false,
        token: 'mock-token',
        user: {
          id: 'user_01',
          email: body.email ?? 'care@eloquia.com',
          name: 'Dr. Avery Shaw',
        },
      });
    }

    if (method === 'POST' && url.includes('/sign-up/email')) {
      if (mode === 'existing-user') {
        return jsonResponse(
          {
            message: 'User already exists. Use another email.',
            code: 'USER_ALREADY_EXISTS',
          },
          409,
          'Conflict'
        );
      }

      return jsonResponse({
        token: 'mock-token',
        user: {
          id: 'user_02',
          email: body.email ?? 'care@eloquia.com',
          name: body.name ?? 'Dr. Avery Shaw',
        },
      });
    }

    return jsonResponse(
      {
        message: 'Not found',
      },
      404,
      'Not Found'
    );
  };
};

export const createMockAuthClient = (mode: MockAuthMode): AuthClientInstance =>
  createAuthClient({
    basePath: '/api/auth',
    fetchOptions: {
      throw: false,
      customFetchImpl: createMockAuthFetch(mode),
    },
  });
