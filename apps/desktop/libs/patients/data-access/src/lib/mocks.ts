import {
  ApproveDoctorPatientLinkResponseDto,
  DoctorPatientLink,
  Entry,
  GetAnalyticsResponseDto,
  GetEntriesRequestDto,
  GetEntriesResponseDto,
  GetPatientsRequestDto,
  GetPatientsResponseDto,
  Id,
  IsoDateString,
  PageInfo,
  Patient,
} from '@org/models';

const MOCK_NOW: IsoDateString = '2025-02-01T09:00:00.000Z';

const mockPatients: Patient[] = [
  {
    id: 'patient-1',
    practiceId: 'practice-1',
    firstName: 'Lena',
    lastName: 'Hart',
    dateOfBirth: '1992-03-08',
    status: 'active',
    primaryDoctorId: 'doctor-1',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'patient-2',
    practiceId: 'practice-1',
    firstName: 'Micah',
    lastName: 'Voss',
    dateOfBirth: '1985-11-22',
    status: 'active',
    primaryDoctorId: 'doctor-1',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'patient-3',
    practiceId: 'practice-1',
    firstName: 'Noor',
    lastName: 'Patel',
    dateOfBirth: '1978-07-14',
    status: 'inactive',
    primaryDoctorId: 'doctor-1',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
];

const mockLinks: DoctorPatientLink[] = [
  {
    id: 'link-1',
    doctorId: 'doctor-1',
    patientId: 'patient-2',
    type: 'invite',
    status: 'invited',
    requestedById: 'doctor-1',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'link-2',
    doctorId: 'doctor-2',
    patientId: 'patient-3',
    type: 'request',
    status: 'requested',
    requestedById: 'doctor-2',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
];

const mockEntries: Entry[] = [
  {
    id: 'entry-1',
    patientId: 'patient-1',
    authorId: 'doctor-1',
    type: 'note',
    status: 'final',
    title: 'Intake summary',
    summary: 'Patient onboarding complete with baseline vitals.',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'entry-2',
    patientId: 'patient-1',
    authorId: 'doctor-1',
    type: 'plan',
    status: 'draft',
    title: 'Care plan draft',
    summary: 'Working on a 90-day recovery plan.',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'entry-3',
    patientId: 'patient-2',
    authorId: 'doctor-1',
    type: 'assessment',
    status: 'final',
    title: 'Follow-up assessment',
    summary: 'Symptoms improving with therapy adjustments.',
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
];

const buildPageInfo = (
  total: number,
  page: number,
  pageSize: number
): PageInfo => ({
  page,
  pageSize,
  total,
  totalPages: Math.max(1, Math.ceil(total / pageSize)),
});

const matchesSearch = (patient: Patient, search: string): boolean => {
  const term = search.toLowerCase();
  return (
    patient.firstName.toLowerCase().includes(term) ||
    patient.lastName.toLowerCase().includes(term)
  );
};

export const getMockPatients = (
  query?: GetPatientsRequestDto
): GetPatientsResponseDto => {
  const page = query?.page ?? 1;
  const pageSize = query?.pageSize ?? 10;

  let result = [...mockPatients];

  if (query?.status) {
    result = result.filter((patient) => patient.status === query.status);
  }

  if (query?.search) {
    result = result.filter((patient) => matchesSearch(patient, query.search));
  }

  const start = Math.max(0, (page - 1) * pageSize);
  const paged = result.slice(start, start + pageSize);

  return {
    patients: paged,
    pageInfo: buildPageInfo(result.length, page, pageSize),
  };
};

export const getMockEntries = (
  patientId: Id,
  query?: Omit<GetEntriesRequestDto, 'patientId'>
): GetEntriesResponseDto => {
  const page = query?.page ?? 1;
  const pageSize = query?.pageSize ?? 10;

  let result = mockEntries.filter((entry) => entry.patientId === patientId);

  if (query?.status) {
    result = result.filter((entry) => entry.status === query.status);
  }

  const start = Math.max(0, (page - 1) * pageSize);
  const paged = result.slice(start, start + pageSize);

  return {
    entries: paged,
    pageInfo: buildPageInfo(result.length, page, pageSize),
  };
};

export const getMockAnalytics = (
  patientId: Id,
  range: { from: IsoDateString; to: IsoDateString }
): GetAnalyticsResponseDto => {
  const patientEntries = mockEntries.filter(
    (entry) => entry.patientId === patientId
  ).length;
  const pendingLinks = mockLinks.filter(
    (link) => link.patientId === patientId && link.status !== 'approved'
  ).length;
  const activePatients = mockPatients.filter(
    (patient) => patient.status === 'active'
  ).length;

  return {
    metrics: {
      totalPatients: mockPatients.length,
      activePatients,
      totalEntries: mockEntries.length,
      entriesInRange: patientEntries,
      pendingLinks,
    },
    generatedAt: range.to || MOCK_NOW,
  };
};

export const approveMockLink = (id: Id): ApproveDoctorPatientLinkResponseDto => {
  const link =
    mockLinks.find((item) => item.id === id) ??
    mockLinks[0] ?? {
      id,
      doctorId: 'doctor-1',
      patientId: 'patient-1',
      type: 'invite',
      status: 'invited',
      requestedById: 'doctor-1',
      createdAt: MOCK_NOW,
      updatedAt: MOCK_NOW,
    };

  return {
    link: {
      ...link,
      status: 'approved',
      updatedAt: MOCK_NOW,
    },
  };
};
