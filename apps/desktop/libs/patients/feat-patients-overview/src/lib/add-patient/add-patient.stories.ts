import type { Meta, StoryObj } from '@storybook/angular';
import { applicationConfig } from '@storybook/angular';
import { expect, fn, userEvent, waitFor, within } from 'storybook/test';

import { AddPatient } from './add-patient';

import {
  provideAddPatientMocks,
  scenarioConnected,
  scenarioFailure,
  scenarioLoading,
  scenarioRegenerate,
  scenarioWaiting,
} from '@org/mock-api';

function ensureClipboardMock() {
  const nav = globalThis.navigator as any;
  if (nav?.clipboard?.writeText) return;

  Object.defineProperty(globalThis.navigator, 'clipboard', {
    configurable: true,
    value: {
      writeText: fn(async () => undefined),
    },
  });
}

const meta: Meta<AddPatient> = {
  title: 'Patients/Add Patient',
  component: AddPatient,
  parameters: {
    layout: 'padded',
  },
};

export default meta;

type Story = StoryObj<AddPatient>;

export const Waiting: Story = {
  decorators: [
    applicationConfig({
      providers: provideAddPatientMocks(scenarioWaiting({ code: '123456' }))
        .providers,
    }),
  ],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await expect(canvas.getByText('Add patient')).toBeTruthy();
    await expect(canvas.getByText(/expires in/i)).toBeTruthy();
    await expect(
      canvas.getByText(/waiting for the patient to connect/i),
    ).toBeTruthy();

    const input = canvas.getByLabelText(/pairing code/i) as HTMLInputElement;
    await expect(input.value).toBe('123456');
  },
};

export const Loading: Story = {
  decorators: [
    applicationConfig({
      providers: provideAddPatientMocks(scenarioLoading({ delayMs: 2500 }))
        .providers,
    }),
  ],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const regenerate = canvas.getByRole('button', { name: /generating/i });
    await expect(regenerate).toBeDisabled();
  },
};

export const Connected: Story = {
  decorators: [
    applicationConfig({
      providers: provideAddPatientMocks(scenarioConnected({ code: '123456' }))
        .providers,
    }),
  ],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    await waitFor(() =>
      expect(
        canvas.getByText(/patient connected\. closing shortly\./i),
      ).toBeTruthy(),
    );
  },
};

export const Failure: Story = {
  decorators: [
    applicationConfig({
      providers: provideAddPatientMocks(
        scenarioFailure({ errorMessage: 'Could not reach server' }),
      ).providers,
    }),
  ],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const input = canvas.getByLabelText(/pairing code/i) as HTMLInputElement;
    await expect(input.value).toBe('');
    await expect(
      canvas.getByRole('button', { name: /regenerate/i }),
    ).toBeTruthy();
  },
};

export const CopyCode: Story = {
  decorators: [
    applicationConfig({
      providers: provideAddPatientMocks(scenarioWaiting({ code: '123456' }))
        .providers,
    }),
  ],
  play: async ({ canvasElement }) => {
    ensureClipboardMock();
    const writeTextMock = (globalThis.navigator as any).clipboard.writeText;

    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: /copy/i }));

    await waitFor(() => expect(writeTextMock).toHaveBeenCalled());
  },
};

export const RegenerateFlow: Story = {
  decorators: [
    applicationConfig({
      providers: provideAddPatientMocks(
        scenarioRegenerate({ firstCode: '111111', secondCode: '222222' }),
      ).providers,
    }),
  ],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const input = canvas.getByLabelText(/pairing code/i) as HTMLInputElement;
    await waitFor(() => expect(input.value).toBe('111111'));

    await userEvent.click(canvas.getByRole('button', { name: /regenerate/i }));
    await waitFor(() => expect(input.value).toBe('222222'));
  },
};
