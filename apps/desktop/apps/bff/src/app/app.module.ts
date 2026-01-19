import { Module } from '@nestjs/common';
import { AuthController } from './auth.controller';
import { HealthController } from './health.controller';
import { PatientsController } from './patients.controller';
import { EntriesController } from './entries.controller';
import { EntriesRecentController } from './entries-recent.controller';
import { AnalyticsController } from './analytics.controller';
import { PracticeController } from './practice.controller';
import { LinksController } from './links.controller';
import { BffService } from './bff.service';

@Module({
  controllers: [
    AuthController,
    PatientsController,
    EntriesController,
    EntriesRecentController,
    AnalyticsController,
    PracticeController,
    LinksController,
    HealthController,
  ],
  providers: [BffService],
})
export class AppModule {}
