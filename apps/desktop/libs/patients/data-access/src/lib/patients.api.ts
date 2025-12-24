import { Injectable } from '@angular/core';
import {
  AnalyticsRange,
  GetEntriesRequestDto,
  GetEntriesResponseDto,
  GetPatientsResponseDto,
  GetAnalyticsResponseDto,
  Id,
  LinkApproveResponseDto,
} from '@org/models';
import { ApiClient, environment } from '@org/util';
import {
  approveMockLink,
  getMockAnalytics,
  getMockEntries,
  getMockPatients,
} from './mocks';

export type EntriesQuery = GetEntriesRequestDto;

@Injectable({ providedIn: 'root' })
export class PatientsApi {
  private readonly client = new ApiClient();

  async getPatients(): Promise<GetPatientsResponseDto> {
    if (environment.useMocks) {
      return getMockPatients();
    }

    return this.client.get<GetPatientsResponseDto>('/patients');
  }

  async approveLink(id: Id): Promise<LinkApproveResponseDto> {
    if (environment.useMocks) {
      return approveMockLink(id);
    }

    return this.client.post<LinkApproveResponseDto>(`/links/${id}/approve`);
  }

  async getEntries(
    patientId: Id,
    query?: EntriesQuery,
  ): Promise<GetEntriesResponseDto> {
    if (environment.useMocks) {
      return getMockEntries(patientId, query);
    }

    return this.client.get<GetEntriesResponseDto>(
      `/patients/${patientId}/entries`,
      query,
    );
  }

  async getAnalytics(
    patientId: Id,
    range?: AnalyticsRange,
  ): Promise<GetAnalyticsResponseDto> {
    if (environment.useMocks) {
      return getMockAnalytics(patientId, range);
    }

    return this.client.get<GetAnalyticsResponseDto>(
      `/patients/${patientId}/analytics`,
      range ? { range } : undefined,
    );
  }
}
