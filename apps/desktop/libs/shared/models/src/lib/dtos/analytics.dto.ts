import { IsoDateString } from '../types';

export type AnalyticsRange = '7' | '30' | '90';

export interface GetAnalyticsRequestDto {
  range?: AnalyticsRange;
}

export interface TrendPoint {
  date: IsoDateString;
  avgStutterFrequency: number;
  count: number;
}

export interface AnalyticsDistributions {
  emotions: Record<string, number>;
  triggers: Record<string, number>;
  techniques: Record<string, number>;
}

export interface GetAnalyticsResponseDto {
  rangeDays: number;
  distributions: AnalyticsDistributions;
  trend: TrendPoint[];
}
