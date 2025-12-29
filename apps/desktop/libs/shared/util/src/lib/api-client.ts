import { ApiError } from './api-error';

export type QueryValue = string | number | boolean | null | undefined;
export type QueryParams = Record<string, QueryValue>;

export interface ApiClientOptions {
  baseUrl?: string;
  defaultHeaders?: Record<string, string>;
  dropEmptyStringParams?: boolean;
}

// TODO: Extremely ugly hack, I don't think we even need the metaEnv bundlers
function readEnv(name: string): string | undefined {
  // Vite / modern bundlers
  const metaEnv =
    typeof import.meta !== 'undefined'
      ? (import.meta as { env?: Record<string, unknown> }).env
      : undefined;

  const fromMeta =
    metaEnv && typeof metaEnv === 'object' ? metaEnv[name] : undefined;
  if (typeof fromMeta === 'string' && fromMeta.trim()) return fromMeta.trim();

  // Node / SSR
  const fromProcess =
    typeof process !== 'undefined' && process.env && process.env[name];

  if (typeof fromProcess === 'string' && fromProcess.trim())
    return fromProcess.trim();

  return undefined;
}

function normalizeBaseUrl(url: string): string {
  const trimmed = url.trim();
  const withScheme = /^https?:\/\//i.test(trimmed)
    ? trimmed
    : `https://${trimmed}`;
  return withScheme.replace(/\/+$/, '');
}

export function resolveApiBaseUrl(explicit?: string): string {
  const raw =
    explicit ??
    readEnv('NX_API_BASE_URL') ??
    readEnv('API_BASE_URL') ??
    'http://localhost:8080';

  return normalizeBaseUrl(raw);
}

function buildUrl(
  baseUrl: string,
  path: string,
  params?: QueryParams,
  dropEmpty = false,
): string {
  const isAbsolute = /^https?:\/\//i.test(path);
  const url = new URL(
    isAbsolute ? path : `${baseUrl}${path.startsWith('/') ? '' : '/'}${path}`,
  );

  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value == null) continue; // null or undefined
      if (dropEmpty && value === '') continue;
      url.searchParams.set(key, String(value)); // keeps 0 and false
    }
  }

  return url.toString();
}

function mergeHeaders(
  defaults: Record<string, string>,
  incoming?: HeadersInit,
): Headers {
  const h = new Headers(defaults);
  if (incoming) {
    new Headers(incoming).forEach((v, k) => h.set(k, v));
  }
  return h;
}

function isBodyPassThrough(body: unknown): body is BodyInit {
  return (
    typeof body === 'string' ||
    body instanceof FormData ||
    body instanceof Blob ||
    body instanceof ArrayBuffer ||
    body instanceof URLSearchParams
  );
}

export class ApiClient {
  private readonly baseUrl: string;
  private readonly defaultHeaders: Record<string, string>;
  private readonly dropEmptyStringParams: boolean;

  constructor(options: ApiClientOptions = {}) {
    this.baseUrl = resolveApiBaseUrl(options.baseUrl);
    this.defaultHeaders = options.defaultHeaders ?? {};
    this.dropEmptyStringParams = options.dropEmptyStringParams ?? false;

    console.log(`BaseUrl is: ${this.baseUrl}`);
  }

  get<T>(path: string, params?: QueryParams, init?: RequestInit): Promise<T> {
    const url = buildUrl(
      this.baseUrl,
      path,
      params,
      this.dropEmptyStringParams,
    );
    return this.request<T>({ method: 'GET', url, init });
  }

  post<T, TBody = unknown>(
    path: string,
    body?: TBody,
    init?: RequestInit,
  ): Promise<T> {
    const url = buildUrl(this.baseUrl, path);
    return this.request<T>({ method: 'POST', url, body, init });
  }

  private async request<T>(args: {
    method: 'GET' | 'POST';
    url: string;
    body?: unknown;
    init?: RequestInit;
  }): Promise<T> {
    const { method, url, body, init } = args;

    const headers = mergeHeaders(this.defaultHeaders, init?.headers);

    let requestBody: BodyInit | undefined;
    if (body !== undefined) {
      if (isBodyPassThrough(body)) {
        requestBody = body;
      } else {
        requestBody = JSON.stringify(body);
        headers.set(
          'Content-Type',
          headers.get('Content-Type') ?? 'application/json',
        );
      }
    }

    headers.set('Accept', headers.get('Accept') ?? 'application/json');

    const res = await fetch(url, {
      ...init,
      method,
      credentials: init?.credentials ?? 'include',
      headers,
      body: requestBody,
    });

    const payload = await this.readPayload(res);

    if (!res.ok) {
      throw new ApiError(res.status, payload, res.statusText);
    }

    return payload as T;
  }

  private async readPayload(res: Response): Promise<unknown> {
    if (res.status === 204) return undefined;

    const ct = res.headers.get('content-type') ?? '';
    if (ct.includes('application/json')) {
      try {
        return await res.json();
      } catch {
        return undefined;
      }
    }

    const text = await res.text();
    return text.length ? text : undefined;
  }
}
