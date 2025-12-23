import { bootstrapApplication } from '@angular/platform-browser';
import { environment, resolveUseMocksFlag } from '@org/util';
import { appConfig } from './app/app.config';
import { App } from './app/app';

environment.useMocks = resolveUseMocksFlag();

bootstrapApplication(App, appConfig).catch((err) => console.error(err));
