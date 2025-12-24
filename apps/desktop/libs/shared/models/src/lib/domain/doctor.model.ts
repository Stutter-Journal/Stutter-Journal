import { Id } from '../types';

export type DoctorRole = 'Owner' | 'Staff';

export interface Doctor {
  id: Id;
  email: string;
  displayName: string;
  role: DoctorRole;
  practiceId?: Id | null;
}
