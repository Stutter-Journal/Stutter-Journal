import { Patient, PatientStatus } from '../domain/patient.model';
import { Id, PageInfo } from '../types';

export interface GetPatientsRequestDto {
  practiceId?: Id;
  search?: string;
  status?: PatientStatus;
  page?: number;
  pageSize?: number;
}

export interface GetPatientsResponseDto {
  patients: Patient[];
  pageInfo: PageInfo;
}
