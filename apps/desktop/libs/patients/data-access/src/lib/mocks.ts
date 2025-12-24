import {
  AnalyticsRange,
  Entry,
  GetAnalyticsResponseDto,
  GetEntriesRequestDto,
  GetEntriesResponseDto,
  GetPatientsRequestDto,
  GetPatientsResponseDto,
  Id,
  IsoDateString,
  Link,
  LinkApproveResponseDto,
  Patient,
  TrendPoint,
} from '@org/models';

const MOCK_NOW: IsoDateString = '2025-02-01T09:00:00.000Z';

const mockPatients: Patient[] = [
  {
    id: 'patient-1',
    displayName: 'Lena Hart',
    email: 'lena.hart@example.com',
    patientCode: 'LENA-1',
  },
  {
    id: 'patient-2',
    displayName: 'Micah Voss',
    email: 'micah.voss@example.com',
    patientCode: 'MICAH-2',
  },
  {
    id: 'patient-3',
    displayName: 'Noor Patel',
    email: 'noor.patel@example.com',
    patientCode: 'NOOR-3',
  },
];

const mockLinks: Link[] = [
  {
    id: 'link-1',
    doctorId: 'doctor-1',
    patientId: 'patient-2',
    status: 'Pending',
    requestedAt: MOCK_NOW,
    approvedAt: null,
  },
  {
    id: 'link-2',
    doctorId: 'doctor-2',
    patientId: 'patient-3',
    status: 'Approved',
    requestedAt: MOCK_NOW,
    approvedAt: MOCK_NOW,
  },
];

const mockEntries: Entry[] = [
  {
    id: 'entry-1',
    patientId: 'patient-1',
    happenedAt: '2025-01-30T10:00:00.000Z',
    situation: 'Team meeting',
    emotions: ['anxious', 'tense'],
    triggers: ['public speaking'],
    techniques: ['breathing'],
    stutterFrequency: 4,
    notes: 'Used breathing exercises to slow pace.',
    tags: ['work'],
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'entry-2',
    patientId: 'patient-1',
    happenedAt: '2025-01-25T14:30:00.000Z',
    situation: 'Cafe order',
    emotions: ['calm'],
    triggers: ['ordering'],
    techniques: ['pausing'],
    stutterFrequency: 1,
    notes: 'Felt more relaxed after warm-up.',
    tags: ['daily'],
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
  {
    id: 'entry-3',
    patientId: 'patient-2',
    happenedAt: '2025-01-28T18:00:00.000Z',
    situation: 'Video call with friend',
    emotions: ['comfortable'],
    triggers: ['video call'],
    techniques: ['slow rate'],
    stutterFrequency: 2,
    notes: 'Minor blocks; techniques helped.',
    tags: ['social'],
    createdAt: MOCK_NOW,
    updatedAt: MOCK_NOW,
  },
];

const toStringArray = (value: unknown): string[] => {
  if (Array.isArray(value)) {
    return value.map((item) => String(item));
  }
  if (typeof value === 'string') {
    return [value];
  }
  return [];
};

const isWithinBounds = (
  dateString: IsoDateString,
  from?: IsoDateString,
  to?: IsoDateString,
): boolean => {
  const value = new Date(dateString).getTime();

  if (Number.isNaN(value)) {
    return false;
  }

  if (from && value < new Date(from).getTime()) {
    return false;
  }

  if (to && value > new Date(to).getTime()) {
    return false;
  }

  return true;
};

const buildDistributions = (entries: Entry[]) => {
  const distributions = {
    emotions: {} as Record<string, number>,
    triggers: {} as Record<string, number>,
    techniques: {} as Record<string, number>,
  };

  const incrementCounts = (
    target: Record<string, number>,
    values: string[],
  ) => {
    for (const value of values) {
      target[value] = (target[value] ?? 0) + 1;
    }
  };

  for (const entry of entries) {
    incrementCounts(distributions.emotions, toStringArray(entry.emotions));
    incrementCounts(distributions.triggers, toStringArray(entry.triggers));
    incrementCounts(distributions.techniques, toStringArray(entry.techniques));
  }

  return distributions;
};

const buildTrend = (entries: Entry[]): TrendPoint[] => {
  const buckets = new Map<string, number[]>();

  for (const entry of entries) {
    const date = entry.happenedAt.slice(0, 10);
    const frequencies = buckets.get(date) ?? [];
    frequencies.push(entry.stutterFrequency ?? 0);
    buckets.set(date, frequencies);
  }

  return Array.from(buckets.entries())
    .sort(([a], [b]) => (a > b ? 1 : -1))
    .map(([date, frequencies]) => {
      const count = frequencies.length;
      const total = frequencies.reduce((sum, value) => sum + value, 0);
      const avg = count === 0 ? 0 : Number((total / count).toFixed(2));
      return { date, avgStutterFrequency: avg, count };
    });
};

export const getMockPatients = (
  _query?: GetPatientsRequestDto,
): GetPatientsResponseDto => {
  const pendingLinks = mockLinks.filter((link) => link.status === 'Pending');
  return { patients: mockPatients, pendingLinks };
};

export const getMockEntries = (
  patientId: Id,
  query?: GetEntriesRequestDto,
): GetEntriesResponseDto => {
  const entries = mockEntries.filter(
    (entry) =>
      entry.patientId === patientId &&
      isWithinBounds(entry.happenedAt, query?.from, query?.to),
  );

  return { entries };
};

export const getMockAnalytics = (
  patientId: Id,
  range?: AnalyticsRange,
): GetAnalyticsResponseDto => {
  const rangeDays = range ? Number(range) : 7;

  const end = new Date(MOCK_NOW);
  const start = new Date(end);
  start.setDate(end.getDate() - rangeDays + 1);

  const filtered = mockEntries.filter(
    (entry) =>
      entry.patientId === patientId &&
      isWithinBounds(
        entry.happenedAt,
        start.toISOString() as IsoDateString,
        end.toISOString() as IsoDateString,
      ),
  );

  return {
    rangeDays,
    distributions: buildDistributions(filtered),
    trend: buildTrend(filtered),
  };
};

export const approveMockLink = (id: Id): LinkApproveResponseDto => {
  const existing =
    mockLinks.find((item) => item.id === id) ??
    ({
      id,
      doctorId: 'doctor-1',
      patientId: mockPatients[0]?.id ?? 'patient-unknown',
      status: 'Pending',
      requestedAt: MOCK_NOW,
      approvedAt: null,
    } satisfies Link);

  const link: Link = {
    ...existing,
    status: 'Approved',
    approvedAt: MOCK_NOW,
  };

  const patient =
    mockPatients.find((item) => item.id === link.patientId) ?? mockPatients[0];

  return { link, patient };
};
