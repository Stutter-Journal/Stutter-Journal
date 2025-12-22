import { Id, IsoDateString } from '../types';

export type PracticeStatus = 'active' | 'inactive';

export interface Practice {
  id: Id;
  name: string;
  slug: string;
  timezone: string;
  status: PracticeStatus;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
