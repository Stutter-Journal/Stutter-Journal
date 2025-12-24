import { Id, IsoDateString } from '../types';

export type LinkStatus = 'Pending' | 'Approved' | 'Denied' | 'Revoked';

export interface Link {
  id: Id;
  doctorId: Id;
  patientId: Id;
  status: LinkStatus;
  requestedAt: IsoDateString;
  approvedAt?: IsoDateString | null;
}
