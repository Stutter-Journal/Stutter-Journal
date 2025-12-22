import { Practice } from '../domain/practice.model';
import { Id } from '../types';

export interface CreatePracticeRequestDto {
  name: string;
  slug?: string;
  timezone?: string;
  ownerId?: Id;
}

export interface CreatePracticeResponseDto {
  practice: Practice;
}
