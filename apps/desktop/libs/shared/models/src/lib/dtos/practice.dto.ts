import { Doctor } from '../domain/doctor.model';
import { Practice } from '../domain/practice.model';

export interface CreatePracticeRequestDto {
  name: string;
  address?: string | null;
  logoUrl?: string | null;
}

export interface CreatePracticeResponseDto {
  practice: Practice;
  doctor: Doctor;
}
