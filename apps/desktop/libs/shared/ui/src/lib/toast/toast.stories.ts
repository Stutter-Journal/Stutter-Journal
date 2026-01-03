import type { Meta, StoryObj } from '@storybook/angular';
import { moduleMetadata } from '@storybook/angular';
import { toast } from 'ngx-sonner';
import { Toast } from './toast';

const meta: Meta<Toast> = {
  title: 'UI/Toast',
  component: Toast,
  decorators: [
    moduleMetadata({
      imports: [Toast],
    }),
  ],
  argTypes: {
    theme: {
      control: 'select',
      options: ['light', 'dark', 'auto'],
    },
    position: {
      control: 'select',
      options: [
        'top-left',
        'top-center',
        'top-right',
        'bottom-left',
        'bottom-center',
        'bottom-right',
      ],
    },
    richColors: { control: 'boolean' },
    expand: { control: 'boolean' },
    closeButton: { control: 'boolean' },
    invert: { control: 'boolean' },
    duration: { control: 'number' },
    visibleToasts: { control: 'number' },
  },
  args: {
    theme: 'light',
    position: 'bottom-right',
    duration: 4000,
    visibleToasts: 3,
    richColors: false,
    expand: false,
    closeButton: false,
    invert: false,
    dir: 'auto',
    hotKey: ['altKey', 'KeyT'],
    toastOptions: {},
    offset: null,
  },
};

export default meta;
type Story = StoryObj<Toast>;

const template = `
  <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
    <button type="button" (click)="fireInfo()">Info</button>
    <button type="button" (click)="fireSuccess()">Success</button>
    <button type="button" (click)="fireError()">Error</button>
  </div>
  <lib-toast
    [theme]="theme"
    [position]="position"
    [duration]="duration"
    [visibleToasts]="visibleToasts"
    [richColors]="richColors"
    [expand]="expand"
    [closeButton]="closeButton"
    [invert]="invert"
    [hotKey]="hotKey"
    [toastOptions]="toastOptions"
    [offset]="offset"
    [dir]="dir"
  />
`;

export const Playground: Story = {
  render: (args) => ({
    props: {
      ...args,
      fireInfo: () => toast('Heads up!', { description: 'This is a toast message.' }),
      fireSuccess: () => toast.success('Saved!', { description: 'Your changes were saved successfully.' }),
      fireError: () => toast.error('Something went wrong', { description: 'Please try again.' }),
    },
    template,
  }),
};
