import type { Meta, StoryObj } from '@storybook/angular';
import { FeatPatientsOverview } from './feat-patients-overview';
import { expect } from 'storybook/test';

const meta: Meta<FeatPatientsOverview> = {
  component: FeatPatientsOverview,
  title: 'FeatPatientsOverview',
};
export default meta;

type Story = StoryObj<FeatPatientsOverview>;

export const Primary: Story = {
  args: {},
};

export const Heading: Story = {
  args: {},
  play: async ({ canvas }) => {
    await expect(canvas.getByText(/feat-patients-overview/gi)).toBeTruthy();
  },
};
