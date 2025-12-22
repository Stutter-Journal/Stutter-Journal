import { Entry, EntryStatus } from '../domain/entry.model';
import { Id, PageInfo } from '../types';

export interface GetEntriesRequestDto {
  patientId?: Id;
  authorId?: Id;
  status?: EntryStatus;
  page?: number;
  pageSize?: number;
}

export interface GetEntriesResponseDto {
  entries: Entry[];
  pageInfo: PageInfo;
}
