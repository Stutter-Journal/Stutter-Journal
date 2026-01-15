import type { Provider } from '@angular/core';

import type {
  ServerPairingCodeCreateResponse,
  ServerPatientsResponse,
} from '@org/contracts';
import { LinksClientService } from '@org/links-data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { LoggerService } from '@org/util';

import { BrnDialogRef } from '@spartan-ng/brain/dialog';

import {
  createAsyncSequence,
  delay,
  type AsyncSequenceItem,
} from './async-sequence';
import type { AddPatientMockScenario } from './add-patient.scenarios';

export interface AddPatientMockRefs {
  dialogRef: { closeCalls: number };
  links: { createPairingCodeCalls: number };
  patients: { getPatientsResponseCalls: number };
}

function toAsyncItems<T>(items: Array<T | Error>): AsyncSequenceItem<T>[] {
  return items.map((i) => i);
}

export function provideAddPatientMocks(
  scenario: AddPatientMockScenario,
  refs?: Partial<AddPatientMockRefs>,
): { providers: Provider[]; refs: AddPatientMockRefs } {
  const dialogRefState = refs?.dialogRef ?? { closeCalls: 0 };
  const linksState = refs?.links ?? { createPairingCodeCalls: 0 };
  const patientsState = refs?.patients ?? { getPatientsResponseCalls: 0 };

  const nextPairing = createAsyncSequence<ServerPairingCodeCreateResponse>(
    toAsyncItems(scenario.pairingCode?.responses ?? []),
    {
      repeatLast: true,
      fallback: new Error('No pairingCode.responses provided'),
    },
  );

  const nextPatients = createAsyncSequence<ServerPatientsResponse>(
    toAsyncItems(scenario.patients?.responses ?? []),
    {
      repeatLast: true,
      fallback: { patients: [] },
    },
  );

  const linksMock: Partial<LinksClientService> = {
    clearError: () => {
      // no-op for story
    },
    createPairingCode: async () => {
      linksState.createPairingCodeCalls += 1;
      const delayMs = scenario.pairingCode?.delayMs;
      if (typeof delayMs === 'number' && delayMs > 0) {
        await delay(delayMs);
      }
      return await nextPairing();
    },
  };

  const patientsMock: Partial<PatientsClientService> = {
    getPatientsResponse: async () => {
      patientsState.getPatientsResponseCalls += 1;
      return await nextPatients();
    },
  };

  const loggerMock: Partial<LoggerService> = {
    debug: () => undefined,
    info: () => undefined,
    warn: () => undefined,
    error: () => undefined,
  };

  const dialogRefMock: Partial<BrnDialogRef<void>> = {
    close: () => {
      dialogRefState.closeCalls += 1;
    },
  };

  return {
    providers: [
      { provide: LinksClientService, useValue: linksMock },
      { provide: PatientsClientService, useValue: patientsMock },
      { provide: LoggerService, useValue: loggerMock },
      { provide: BrnDialogRef, useValue: dialogRefMock },
    ],
    refs: {
      dialogRef: dialogRefState,
      links: linksState,
      patients: patientsState,
    },
  };
}
