import { Id, IsoDateString } from '../types';

export interface Comment {
  id: Id;
  entryId: Id;
  authorId: Id;
  body: string;
  createdAt: IsoDateString;
}
