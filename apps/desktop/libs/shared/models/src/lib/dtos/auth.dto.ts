import { Doctor } from '../domain/doctor.model';
import { Id } from '../types';

export interface RegisterRequestDto {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  practiceId?: Id;
}

export interface RegisterResponseDto {
  doctor: Doctor;
  accessToken: string;
  refreshToken?: string;
}

export interface LoginRequestDto {
  email: string;
  password: string;
}

export interface LoginResponseDto {
  doctor: Doctor;
  accessToken: string;
  refreshToken?: string;
}
