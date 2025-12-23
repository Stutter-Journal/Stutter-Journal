import { provideRouter } from '@angular/router';
import type { Meta, StoryObj } from '@storybook/angular';
import { applicationConfig } from '@storybook/angular';
import { expect, userEvent, within } from 'storybook/test';
import { Doctor, Practice } from '@org/models';
import { PracticeApi } from '@org/practice/data-access';
import { OnboardingComponent } from './onboarding.component';

const now = new Date().toISOString();
const mockPractice: Practice = {
  id: 'practice-1',
  name: 'Eloquia Family Clinic',
  slug: 'eloquia-family-clinic',
  timezone: 'UTC',
  status: 'active',
  createdAt: now,
  updatedAt: now,
};
const mockDoctor: Doctor = {
  id: 'doctor-1',
  practiceId: mockPractice.id,
  firstName: 'Avery',
  lastName: 'Reyes',
  email: 'avery@clinic.com',
  role: 'owner',
  status: 'active',
  createdAt: now,
  updatedAt: now,
};

const mockPracticeApi: Pick<PracticeApi, 'createPractice'> = {
  createPractice: async () => ({
    practice: mockPractice,
    doctor: mockDoctor,
  }),
};

const meta: Meta<OnboardingComponent> = {
  component: OnboardingComponent,
  title: 'Portal/Onboarding/Create Practice',
  decorators: [
    applicationConfig({
      providers: [
        provideRouter([]),
        { provide: PracticeApi, useValue: mockPracticeApi },
      ],
    }),
  ],
  argTypes: {
    autoStartTour: { control: 'boolean' },
    initialTourStep: { control: 'number' },
  },
};

export default meta;

type Story = StoryObj<OnboardingComponent>;

export const Default: Story = {
  args: {
    autoStartTour: false,
    initialTourStep: 0,
  },
};

export const ValidationError: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(
      canvas.getByRole('button', { name: /create practice/i })
    );
    await expect(
      canvas.getByText(/practice name is required\./i)
    ).toBeTruthy();
  },
};

export const GuidedTour: Story = {
  args: {
    autoStartTour: true,
    initialTourStep: 1,
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(
      canvas.getByText(/step 2 of/i)
    ).toBeTruthy();
    await userEvent.click(canvas.getByRole('button', { name: /next/i }));
    await expect(
      canvas.getByText(/finish & go to patients/i)
    ).toBeTruthy();
  },
};
