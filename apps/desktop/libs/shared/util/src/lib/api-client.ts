import { ApiError } from './api-error';

export type QueryValue = string | number | boolean | null | undefined;
export type QueryParams = Record<string, QueryValue>;

export interface ApiClientOptions {
  baseUrl?: string;
  defaultHeaders?: Record<string, string>;
}

export class ApiClient {
  private readonly baseUrl: string;
  private readonly defaultHeaders: Record<string, string>;

  constructor(options: ApiClientOptions = {}) {
    this.baseUrl = options.baseUrl ?? '';
    this.defaultHeaders = options.defaultHeaders ?? {};
  }

  async get<T>(path: string, params?: QueryParams, init?: RequestInit): Promise<T> {
    const url = this.buildUrl(path, params);
    return this.request<T>('GET', url, undefined, init);
  }

  async post<T, TBody>(
    path: string,
    body: TBody,
    init?: RequestInit
  ): Promise<T> {
    const url = this.buildUrl(path);
    return this.request<T>('POST', url, body, init);
  }

  private buildUrl(path: string, params?: QueryParams): string {
    const base = this.baseUrl ? this.baseUrl.replace(/\/$/, '') : '';
    const normalizedPath = path.startsWith('http')
      ? path
      : `${base}${path.startsWith('/') ? '' : '/'}${path}`;

    if (!params) {
      return normalizedPath;
    }

    const entries = Object.entries(params).filter(([, value]) =>
      value === 0 ? true : Boolean(value)
    );

    if (entries.length === 0) {
      return normalizedPath;
    }

    const searchParams = new URLSearchParams();
    for (const [key, value] of entries) {
      searchParams.set(key, String(value));
    }

    const separator = normalizedPath.includes('?') ? '&' : '?';
    return `${normalizedPath}${separator}${searchParams.toString()}`;
  }

  private async request<T>(
    method: 'GET' | 'POST',
    url: string,
    body?: unknown,
    init?: RequestInit
  ): Promise<T> {
    const headers = new Headers(this.defaultHeaders);

    if (init?.headers) {
      const incoming = new Headers(init.headers);
      incoming.forEach((value, key) => headers.set(key, value));
    }

    let requestBody: BodyInit | undefined;

    if (body !== undefined) {
      if (
        typeof body === 'string' ||
        body instanceof FormData ||
        body instanceof Blob ||
        body instanceof ArrayBuffer
      ) {
        requestBody = body as BodyInit;
      } else {
        requestBody = JSON.stringify(body);
        if (!headers.has('Content-Type')) {
          headers.set('Content-Type', 'application/json');
        }
      }
    }

    if (!headers.has('Accept')) {
      headers.set('Accept', 'application/json');
    }

    const response = await fetch(url, {
      method,
      credentials: 'include',
      ...init,
      headers,
      body: requestBody,
    });

    const payload = await this.readPayload(response);

    if (!response.ok) {
      throw new ApiError(response.status, payload, response.statusText);
    }

    return payload as T;
  }

  private async readPayload(response: Response): Promise<unknown> {
    if (response.status === 204) {
      return undefined;
    }

    const contentType = response.headers.get('content-type') ?? '';
    const isJson = contentType.includes('application/json');

    if (isJson) {
      try {
        return await response.json();
      } catch {
        return undefined;
      }
    }

    const text = await response.text();
    return text === '' ? undefined : text;
  }
}
