import { Injectable } from '@angular/core';
import {
  CreatePracticeRequestDto,
  CreatePracticeResponseDto,
} from '@org/models';
import { ApiClient } from '@org/util';

@Injectable({ providedIn: 'root' })
export class PracticeApi {
  private readonly client = new ApiClient();

  createPractice(
    payload: CreatePracticeRequestDto,
  ): Promise<CreatePracticeResponseDto> {
    return this.client.post<
      CreatePracticeResponseDto,
      CreatePracticeRequestDto
    >('/practice', payload);
  }
}
