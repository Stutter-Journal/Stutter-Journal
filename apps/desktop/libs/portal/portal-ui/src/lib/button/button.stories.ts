import type { Meta, StoryObj } from '@storybook/angular';
import { ButtonComponent } from './button';
import { expect } from 'storybook/test';

const meta: Meta<ButtonComponent> = {
  component: ButtonComponent,
  title: 'Button',
};
export default meta;

type Story = StoryObj<ButtonComponent>;

export const Primary: Story = {
  args: {
    variant: 'primary',
  },
  render: (args) => ({
    props: args,
    template: '<lib-button [variant]="variant">Primary</lib-button>',
  }),
};

export const Heading: Story = {
  args: {
    variant: 'secondary',
  },
  render: (args) => ({
    props: args,
    template: '<lib-button [variant]="variant">Secondary</lib-button>',
  }),
  play: async ({ canvas }) => {
    await expect(canvas.getByText(/secondary/gi)).toBeTruthy();
  },
};
