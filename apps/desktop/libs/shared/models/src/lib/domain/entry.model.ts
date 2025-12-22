import { Id, IsoDateString } from '../types';

export type EntryType = 'note' | 'assessment' | 'plan';
export type EntryStatus = 'draft' | 'final' | 'amended';

export interface Entry {
  id: Id;
  patientId: Id;
  authorId: Id;
  type: EntryType;
  status: EntryStatus;
  title: string;
  summary?: string;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
