import { Injectable } from '@angular/core';
import {
  ApproveDoctorPatientLinkResponseDto,
  GetEntriesRequestDto,
  GetEntriesResponseDto,
  GetPatientsRequestDto,
  GetPatientsResponseDto,
  GetAnalyticsResponseDto,
  Id,
  IsoDateString,
} from '@org/models';
import { ApiClient, environment } from '@org/util';
import {
  approveMockLink,
  getMockAnalytics,
  getMockEntries,
  getMockPatients,
} from './mocks';

export type EntriesQuery = Omit<GetEntriesRequestDto, 'patientId'>;

export interface AnalyticsRange {
  from: IsoDateString;
  to: IsoDateString;
}

@Injectable({ providedIn: 'root' })
export class PatientsApi {
  private readonly client = new ApiClient();

  async getPatients(
    query?: GetPatientsRequestDto
  ): Promise<GetPatientsResponseDto> {
    if (environment.useMocks) {
      return getMockPatients(query);
    }

    return this.client.get<GetPatientsResponseDto>('/patients', query);
  }

  async approveLink(id: Id): Promise<ApproveDoctorPatientLinkResponseDto> {
    if (environment.useMocks) {
      return approveMockLink(id);
    }

    return this.client.post<ApproveDoctorPatientLinkResponseDto, { linkId: Id }>(
      `/patients/links/${id}/approve`,
      { linkId: id }
    );
  }

  async getEntries(
    patientId: Id,
    query?: EntriesQuery
  ): Promise<GetEntriesResponseDto> {
    if (environment.useMocks) {
      return getMockEntries(patientId, query);
    }

    return this.client.get<GetEntriesResponseDto>(
      `/patients/${patientId}/entries`,
      query
    );
  }

  async getAnalytics(
    patientId: Id,
    range: AnalyticsRange
  ): Promise<GetAnalyticsResponseDto> {
    if (environment.useMocks) {
      return getMockAnalytics(patientId, range);
    }

    return this.client.get<GetAnalyticsResponseDto>(
      `/patients/${patientId}/analytics`,
      range
    );
  }
}
