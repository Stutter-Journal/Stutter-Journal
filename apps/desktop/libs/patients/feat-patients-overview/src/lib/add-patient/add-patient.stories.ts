import type { Meta, StoryObj } from '@storybook/angular';
import { AddPatient } from './add-patient';
import { expect } from 'storybook/test';

const meta: Meta<AddPatient> = {
  component: AddPatient,
  title: 'AddPatient',
};
export default meta;

type Story = StoryObj<AddPatient>;

export const Primary: Story = {
  args: {},
};

export const Heading: Story = {
  args: {},
  play: async ({ canvas }) => {
    await expect(canvas.getByText(/add-patient/gi)).toBeTruthy();
  },
};
