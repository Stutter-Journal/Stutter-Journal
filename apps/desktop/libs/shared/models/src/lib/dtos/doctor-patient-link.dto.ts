import {
  DoctorPatientLink,
  DoctorPatientLinkType,
} from '../domain/doctor-patient-link.model';
import { Id } from '../types';

export interface CreateDoctorPatientLinkRequestDto {
  type: DoctorPatientLinkType;
  doctorId: Id;
  patientId: Id;
  message?: string;
}

export interface CreateDoctorPatientLinkResponseDto {
  link: DoctorPatientLink;
}

export interface ApproveDoctorPatientLinkRequestDto {
  linkId: Id;
  approvedById: Id;
}

export interface ApproveDoctorPatientLinkResponseDto {
  link: DoctorPatientLink;
}
