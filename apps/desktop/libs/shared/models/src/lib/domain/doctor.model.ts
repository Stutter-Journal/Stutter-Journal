import { Id, IsoDateString } from '../types';

export type DoctorRole = 'owner' | 'admin' | 'member';
export type DoctorStatus = 'active' | 'inactive' | 'invited';

export interface Doctor {
  id: Id;
  practiceId: Id;
  firstName: string;
  lastName: string;
  email: string;
  role: DoctorRole;
  status: DoctorStatus;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
