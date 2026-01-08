import { contractsZod } from '@eloquia/shared/api/contracts';
import { z } from 'zod';

// Doctor/Auth (sourced from generated contracts)
export const serverDoctorResponseSchema = contractsZod.postDoctorLoginResponse;
export const serverStatusResponseSchema = contractsZod.getHealthResponse;
export const serverDoctorMeResponseSchema = contractsZod.getDoctorMeResponse;

// Patients
export const serverPatientsResponseSchema = contractsZod.getPatientsResponse;

// Entries
export const serverEntriesResponseSchema = contractsZod.getPatientsIdEntriesResponse;

// Analytics
export const serverAnalyticsResponseSchema = contractsZod.getPatientsIdAnalyticsResponse;

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

export const serverLinkApproveResponseSchema = contractsZod.postLinksIdApproveResponse;

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
  error: z.string().optional(),
});

export type ServerDoctorResponse = z.infer<typeof serverDoctorResponseSchema>;
export type ServerPatientsResponse = z.infer<
  typeof serverPatientsResponseSchema
>;
export type ServerEntriesResponse = z.infer<typeof serverEntriesResponseSchema>;
export type ServerAnalyticsResponse = z.infer<
  typeof serverAnalyticsResponseSchema
>;
export type ServerPracticeResponse = z.infer<
  typeof serverPracticeResponseSchema
>;
export type ServerPracticeCreateResponse = z.infer<
  typeof serverPracticeCreateResponseSchema
>;
export type ServerLinkResponse = z.infer<typeof serverLinkResponseSchema>;
export type ServerLinkApproveResponse = z.infer<
  typeof serverLinkApproveResponseSchema
>;
export type ServerStatusResponse = z.infer<typeof serverStatusResponseSchema>;
export type ServerErrorResponse = z.infer<typeof serverErrorResponseSchema>;
