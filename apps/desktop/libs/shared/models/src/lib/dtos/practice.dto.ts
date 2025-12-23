import { Doctor } from '../domain/doctor.model';
import { Practice } from '../domain/practice.model';
import { Id } from '../types';

export interface CreatePracticeRequestDto {
  name: string;
  slug?: string;
  timezone?: string;
  ownerId?: Id;
  address?: string;
  logoUrl?: string;
}

export interface CreatePracticeResponseDto {
  practice?: Practice;
  doctor?: Doctor;
}
