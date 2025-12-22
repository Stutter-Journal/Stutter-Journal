import { Id, IsoDateString } from '../types';

export type DoctorPatientLinkType = 'invite' | 'request';
export type DoctorPatientLinkStatus =
  | 'requested'
  | 'invited'
  | 'approved'
  | 'declined';

export interface DoctorPatientLink {
  id: Id;
  doctorId: Id;
  patientId: Id;
  type: DoctorPatientLinkType;
  status: DoctorPatientLinkStatus;
  requestedById: Id;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
