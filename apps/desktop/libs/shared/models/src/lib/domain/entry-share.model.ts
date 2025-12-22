import { Id, IsoDateString } from '../types';

export type EntryShareStatus = 'pending' | 'accepted' | 'revoked';

export interface EntryShare {
  id: Id;
  entryId: Id;
  sharedWithDoctorId: Id;
  status: EntryShareStatus;
  createdAt: IsoDateString;
  updatedAt: IsoDateString;
}
