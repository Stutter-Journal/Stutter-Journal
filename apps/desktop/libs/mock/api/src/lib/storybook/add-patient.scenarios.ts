import type {
  ServerPairingCodeCreateResponse,
  ServerPatientsResponse,
} from '@org/contracts';

export interface AddPatientMockScenario {
  pairingCode?: {
    responses: Array<ServerPairingCodeCreateResponse | Error>;
    delayMs?: number;
  };
  patients?: {
    responses: Array<ServerPatientsResponse | Error>;
  };
}

export function pairingResponse(
  code: string,
  options?: { expiresInMs?: number; qrText?: string },
): ServerPairingCodeCreateResponse {
  const expiresInMs = options?.expiresInMs ?? 60_000;

  return {
    code,
    // Important: AddPatient uses `qrText ?? code`; an empty string prevents QR generation
    // while still showing the code in the UI.
    qrText: options?.qrText ?? '',
    expiresAt: new Date(Date.now() + expiresInMs).toISOString(),
  };
}

export function patientsResponse(count: number): ServerPatientsResponse {
  return {
    patients: Array.from({ length: count }, (_, i) => ({ id: String(i + 1) })),
  };
}

export function scenarioWaiting(options?: {
  code?: string;
  expiresInMs?: number;
  baselinePatients?: number;
}): AddPatientMockScenario {
  return {
    pairingCode: {
      responses: [pairingResponse(options?.code ?? '123456', { expiresInMs: options?.expiresInMs })],
    },
    patients: {
      responses: [patientsResponse(options?.baselinePatients ?? 0)],
    },
  };
}

export function scenarioLoading(options?: {
  code?: string;
  delayMs?: number;
}): AddPatientMockScenario {
  return {
    pairingCode: {
      responses: [pairingResponse(options?.code ?? '123456')],
      delayMs: options?.delayMs ?? 2500,
    },
    patients: {
      responses: [patientsResponse(0)],
    },
  };
}

export function scenarioConnected(options?: {
  code?: string;
}): AddPatientMockScenario {
  return {
    pairingCode: {
      responses: [pairingResponse(options?.code ?? '123456')],
    },
    // First call is baseline=0, second call is "increased" => CONNECTED (startWith(0) makes it immediate)
    patients: {
      responses: [patientsResponse(0), patientsResponse(1)],
    },
  };
}

export function scenarioFailure(options?: {
  errorMessage?: string;
  baselinePatients?: number;
}): AddPatientMockScenario {
  return {
    pairingCode: {
      responses: [new Error(options?.errorMessage ?? 'Backend exploded')],
    },
    patients: {
      responses: [patientsResponse(options?.baselinePatients ?? 0)],
    },
  };
}

export function scenarioRegenerate(options?: {
  firstCode?: string;
  secondCode?: string;
}): AddPatientMockScenario {
  return {
    pairingCode: {
      responses: [
        pairingResponse(options?.firstCode ?? '111111'),
        pairingResponse(options?.secondCode ?? '222222'),
      ],
    },
    patients: {
      responses: [patientsResponse(0)],
    },
  };
}
