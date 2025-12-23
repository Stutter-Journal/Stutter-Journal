import { provideRouter } from '@angular/router';
import type { Meta, StoryObj } from '@storybook/angular';
import { applicationConfig } from '@storybook/angular';
import { AUTH_CLIENT } from '../auth-client';
import { createMockAuthClient } from '../auth.mocks';
import { RegisterComponent } from './register.component';

const meta: Meta<RegisterComponent> = {
  component: RegisterComponent,
  title: 'Portal/Auth/Register',
};

export default meta;

type Story = StoryObj<RegisterComponent>;

export const Default: Story = {
  decorators: [
    applicationConfig({
      providers: [
        provideRouter([]),
        { provide: AUTH_CLIENT, useValue: createMockAuthClient('success') },
      ],
    }),
  ],
};
