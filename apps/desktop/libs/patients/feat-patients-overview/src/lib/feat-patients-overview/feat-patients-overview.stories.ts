import type { Meta, StoryObj } from '@storybook/angular';
import { applicationConfig } from '@storybook/angular';
import { FeatPatientsOverview } from './feat-patients-overview';
import { expect } from 'storybook/test';
import { PatientsClientService } from '@org/patients-data-access';
import { computed } from '@angular/core';

const meta: Meta<FeatPatientsOverview> = {
  component: FeatPatientsOverview,
  title: 'FeatPatientsOverview',
};
export default meta;

type Story = StoryObj<FeatPatientsOverview>;

export const Primary: Story = {
  decorators: [
    applicationConfig({
      providers: [
        {
          provide: PatientsClientService,
          useValue: {
            loading: computed(() => false),
            error: computed(() => null),
            getPatientsResponse: async () => ({
              patients: [
                { id: 'p_001', displayName: 'Anna Müller' },
                { id: 'p_002', displayName: 'Lukas Steiner' },
              ],
              pendingLinks: [{ id: 'l_001', patientId: 'p_003', status: 'Pending' }],
            }),
          },
        },
      ],
    }),
  ],
  args: {},
};

export const Heading: Story = {
  decorators: Primary.decorators,
  args: {},
  play: async ({ canvas }) => {
    await expect(canvas.getByText(/patients/gi)).toBeTruthy();
  },
};
