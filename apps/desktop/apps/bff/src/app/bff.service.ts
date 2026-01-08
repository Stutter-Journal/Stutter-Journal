import { Injectable } from '@nestjs/common';
import { z } from 'zod';
import { Request, Response } from 'express';

const BASE_URL = process.env.ELOQUIA_API_BASE_URL || 'http://localhost:8080';

interface ForwardOptions<TBody = unknown> {
  req: Request;
  res: Response;
  path: string;
  method: 'GET' | 'POST';
  body?: TBody;
  schema?: z.ZodTypeAny | null;
  schemasByStatus?: Record<number, z.ZodTypeAny>;
}

@Injectable()
export class BffService {
  /**
   * Forwards to the upstream Go API, preserves cookies, validates response (if schema provided),
   * and returns the upstream status/body to the client.
   */
  async forward<TBody = unknown>({
    req,
    res,
    path,
    method,
    body,
    schema,
    schemasByStatus,
  }: ForwardOptions<TBody>): Promise<void> {
    const url = `${BASE_URL}${path}`;
    const headers: Record<string, string> = {
      'content-type': 'application/json',
    };

    if (req.headers.cookie) {
      headers.cookie = req.headers.cookie;
    }

    const upstream = await fetch(url, {
      method,
      headers,
      redirect: 'manual',
      body: body ? JSON.stringify(body) : undefined,
    });

    this.copySetCookie(upstream, res);

    const text = await upstream.text();
    let json: unknown;
    try {
      json = text ? JSON.parse(text) : undefined;
    } catch (err) {
      json = text; // if upstream returns non-JSON, keep raw text
    }

    const chosenSchema = schemasByStatus?.[upstream.status] ?? schema ?? null;

    if (!chosenSchema) {
      res.status(upstream.status).json(json ?? null);
      return;
    }

    const parsed = chosenSchema.safeParse(json);

    if (parsed.success) {
      res.status(upstream.status).json(parsed.data ?? null);
      return;
    }

    // On validation failure, return the upstream payload but log the mismatch
    console.warn('Upstream response failed validation', {
      path,
      status: upstream.status,
      issues: parsed.error.issues,
    });

    res.status(upstream.status).json(json ?? null);
  }

  private copySetCookie(upstream: Response | any, res: Response) {
    const setCookies: string[] =
      upstream.headers?.getSetCookie?.() ??
      upstream.headers?.raw?.()['set-cookie'] ??
      [];
    setCookies.forEach((cookie) => res.append('set-cookie', cookie));
  }
}
