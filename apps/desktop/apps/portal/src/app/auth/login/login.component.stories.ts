import { provideRouter } from '@angular/router';
import type { Meta, StoryObj } from '@storybook/angular';
import { applicationConfig } from '@storybook/angular';
import { AUTH_CLIENT } from '../auth-client';
import { createMockAuthClient } from '../auth.mocks';
import { LoginComponent } from './login.component';

const meta: Meta<LoginComponent> = {
  component: LoginComponent,
  title: 'Portal/Auth/Login',
};

export default meta;

type Story = StoryObj<LoginComponent>;

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
