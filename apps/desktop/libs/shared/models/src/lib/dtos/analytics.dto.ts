import { Id, IsoDateString } from '../types';

export interface GetAnalyticsRequestDto {
  practiceId: Id;
  from: IsoDateString;
  to: IsoDateString;
}

export interface AnalyticsMetrics {
  totalPatients: number;
  activePatients: number;
  totalEntries: number;
  entriesInRange: number;
  pendingLinks: number;
}

export interface GetAnalyticsResponseDto {
  metrics: AnalyticsMetrics;
  generatedAt: IsoDateString;
}
