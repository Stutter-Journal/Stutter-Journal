import { Link } from '../domain/doctor-patient-link.model';
import { Patient } from '../domain/patient.model';

export type GetPatientsRequestDto = Record<string, never>;

export interface GetPatientsResponseDto {
  patients: Patient[];
  pendingLinks: Link[];
}
