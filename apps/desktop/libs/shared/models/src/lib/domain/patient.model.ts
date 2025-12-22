import { Id, IsoDateString } from '../types';

export type PatientStatus = 'active' | 'inactive' | 'discharged';

export interface Patient {
  id: Id;
  practiceId: Id;
  firstName: string;
  lastName: string;
  dateOfBirth: IsoDateString;
  status: PatientStatus;
  primaryDoctorId?: Id;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
