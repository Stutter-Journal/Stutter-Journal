import { Id } from '../types';

export interface Patient {
  id: Id;
  displayName: string;
  email?: string | null;
  patientCode?: string | null;
}
