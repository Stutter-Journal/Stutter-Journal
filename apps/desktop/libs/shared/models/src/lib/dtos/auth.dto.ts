import { Doctor } from '../domain/doctor.model';

export interface DoctorRegisterRequestDto {
  email: string;
  displayName: string;
  password: string;
}

export interface DoctorLoginRequestDto {
  email: string;
  password: string;
}

export interface DoctorResponseDto {
  doctor: Doctor;
}

export interface StatusResponseDto {
  status: string;
}
