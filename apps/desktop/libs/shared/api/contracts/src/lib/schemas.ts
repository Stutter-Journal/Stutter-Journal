import { z } from 'zod';
import * as contractsZod from '../generated/zod';

// Doctor/Auth (sourced from generated contracts)
export const serverDoctorResponseSchema = contractsZod.postDoctorLoginResponse;

// Links
const serverLinkSchema = z.object({
  id: z.string().optional(),
  doctorId: z.string().optional(),
  patientId: z.string().optional(),
  requestedAt: z.string().optional(),
  approvedAt: z.string().optional(),
  status: z.string().optional(),
});

export const serverLinkResponseSchema = z.object({
  link: serverLinkSchema.optional(),
  patient: z
    .object({
      id: z.string().optional(),
      displayName: z.string().optional(),
      email: z.string().optional(),
      patientCode: z.string().optional(),
    })
    .optional(),
});

// Practice (still manual until added to OpenAPI)
export const serverPracticeResponseSchema = z.object({
  id: z.string().optional(),
  name: z.string().optional(),
  address: z.string().optional(),
  logoUrl: z.string().optional(),
});

export const serverPracticeCreateResponseSchema = z.object({
  practice: serverPracticeResponseSchema.optional(),
  doctor: serverDoctorResponseSchema.optional(),
});

// Errors
export const serverErrorResponseSchema = z.object({
  error: z.string(),
});
