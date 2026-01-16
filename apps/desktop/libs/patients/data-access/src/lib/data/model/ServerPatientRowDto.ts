import { ServerLinkDTO, ServerPatientDTO } from '@org/contracts';

export interface ServerPatientRowDto {
  link?: ServerLinkDTO;
  patient?: ServerPatientDTO;
}
