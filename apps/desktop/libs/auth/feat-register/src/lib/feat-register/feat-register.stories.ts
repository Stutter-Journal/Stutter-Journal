import type { Meta, StoryObj } from '@storybook/angular';
import { FeatRegister } from './feat-register';
import { expect } from 'storybook/test';

const meta: Meta<FeatRegister> = {
  component: FeatRegister,
  title: 'FeatRegister',
};
export default meta;

type Story = StoryObj<FeatRegister>;

export const Primary: Story = {
  args: {},
};

export const Heading: Story = {
  args: {},
  play: async ({ canvas }) => {
    await expect(canvas.getByText(/feat-register/gi)).toBeTruthy();
  },
};
