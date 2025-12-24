import { Id, IsoDateString } from '../types';

export interface Entry {
  id: Id;
  patientId: Id;
  happenedAt: IsoDateString;
  situation?: string | null;
  emotions?: unknown;
  triggers?: unknown;
  techniques?: unknown;
  stutterFrequency?: number | null;
  notes?: string | null;
  tags?: unknown;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
