import type { Meta, StoryObj } from '@storybook/angular';
import { Toast } from './toast';
import { expect } from 'storybook/test';

const meta: Meta<Toast> = {
  component: Toast,
  title: 'Toast',
};
export default meta;

type Story = StoryObj<Toast>;

export const Primary: Story = {
  args: {},
};

export const Heading: Story = {
  args: {},
  play: async ({ canvas }) => {
    await expect(canvas.getByText(/toast/gi)).toBeTruthy();
  },
};
