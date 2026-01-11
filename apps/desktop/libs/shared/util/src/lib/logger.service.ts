import { Injectable, isDevMode } from '@angular/core';
import { LogLevel } from './log-level';

@Injectable({
  providedIn: 'root',
})
export class LoggerService {
  private readonly enabled =
    isDevMode() || (globalThis as any)?.__LOGGING_ENABLED__;

  debug(message: string, context?: unknown) {
    this.log('debug', message, context);
  }

  info(message: string, context?: unknown) {
    this.log('info', message, context);
  }

  warn(message: string, context?: unknown) {
    this.log('warn', message, context);
  }

  error(message: string, context?: unknown) {
    this.log('error', message, context);
  }

  private log(level: LogLevel, message: string, context?: unknown) {
    if (!this.enabled) return;

    const prefix = `[${level.toUpperCase()}]`;

    switch (level) {
      case 'debug':
        console.debug(prefix, message, context ?? '');
        break;
      case 'info':
        console.info(prefix, message, context ?? '');
        break;
      case 'warn':
        console.warn(prefix, message, context ?? '');
        break;
      case 'error':
        console.error(prefix, message, context ?? '');
        break;
    }
  }
}
