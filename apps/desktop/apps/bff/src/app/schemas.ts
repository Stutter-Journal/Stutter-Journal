import { z } from 'zod';

// Doctor/Auth
export const serverDoctorResponseSchema = z.object({
  id: z.string().optional(),
  email: z.string().email().optional(),
  displayName: z.string().optional(),
  practiceId: z.string().optional(),
  role: z.string().optional(),
});

export const serverStatusResponseSchema = z.object({
  status: z.string().optional(),
});

export const serverDoctorMeResponseSchema = z.object({
  doctor: serverDoctorResponseSchema.optional(),
});

// Patients
export const serverPatientSchema = z.object({
  id: z.string().optional(),
  displayName: z.string().optional(),
  email: z.string().optional(),
  patientCode: z.string().optional(),
});

export const serverPatientsResponseSchema = z.object({
  patients: z.array(serverPatientSchema).optional(),
  pendingLinks: z
    .array(
      z.object({
        id: z.string().optional(),
        doctorId: z.string().optional(),
        patientId: z.string().optional(),
        requestedAt: z.string().optional(),
        approvedAt: z.string().optional(),
        status: z.string().optional(),
      }),
    )
    .optional(),
});

// Entries
export const serverEntrySchema = z.object({
  id: z.string().optional(),
  patientId: z.string().optional(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
  emotions: z.any().optional(),
  tags: z.any().optional(),
  techniques: z.any().optional(),
  triggers: z.any().optional(),
  stutterFrequency: z.number().optional(),
  notes: z.string().optional(),
  situation: z.string().optional(),
  happenedAt: z.string().optional(),
});

export const serverEntriesResponseSchema = z.object({
  entries: z.array(serverEntrySchema).optional(),
});

// Analytics
export const serverAnalyticsDistributionsSchema = z.object({
  emotions: z.record(z.string(), z.number()).optional(),
  techniques: z.record(z.string(), z.number()).optional(),
  triggers: z.record(z.string(), z.number()).optional(),
});

export const serverTrendPointSchema = z.object({
  date: z.string().optional(),
  count: z.number().optional(),
  avgStutterFrequency: z.number().optional(),
});

export const serverAnalyticsResponseSchema = z.object({
  distributions: serverAnalyticsDistributionsSchema.optional(),
  rangeDays: z.number().optional(),
  trend: z.array(serverTrendPointSchema).optional(),
});

// Links
export const serverLinkSchema = z.object({
  id: z.string().optional(),
  doctorId: z.string().optional(),
  patientId: z.string().optional(),
  requestedAt: z.string().optional(),
  approvedAt: z.string().optional(),
  status: z.string().optional(),
});

export const serverLinkResponseSchema = z.object({
  link: serverLinkSchema.optional(),
  patient: serverPatientSchema.optional(),
});

export const serverLinkApproveResponseSchema = z.object({
  link: serverLinkSchema.optional(),
  patient: serverPatientSchema.optional(),
});

// Practice
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
