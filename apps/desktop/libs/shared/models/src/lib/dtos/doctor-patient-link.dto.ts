import { Link } from '../domain/doctor-patient-link.model';
import { Patient } from '../domain/patient.model';
import { Id } from '../types';

export interface LinkInviteRequestDto {
  patientId?: Id;
  patientEmail?: string;
  patientCode?: string;
  displayName?: string;
}

export type LinkRequestDto = LinkInviteRequestDto;

export type LinkApproveRequestDto = void;

export interface LinkResponseDto {
  link: Link;
  patient: Patient;
}

export interface LinkApproveResponseDto {
  link: Link;
  patient: Patient;
}
