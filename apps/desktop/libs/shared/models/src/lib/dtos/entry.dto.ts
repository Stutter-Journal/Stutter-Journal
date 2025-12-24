import { Entry } from '../domain/entry.model';
import { IsoDateString } from '../types';

export interface GetEntriesRequestDto {
  from?: IsoDateString;
  to?: IsoDateString;
}

export interface GetEntriesResponseDto {
  entries: Entry[];
}
