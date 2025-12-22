export class ApiError<TPayload = unknown> extends Error {
  readonly status: number;
  readonly payload: TPayload;

  constructor(status: number, payload: TPayload, message?: string) {
    super(message ?? `API Error: ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.payload = payload;
  }
}
