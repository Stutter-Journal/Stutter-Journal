import { ServerDoctorResponse } from '@org/contracts';

export function needsOnboarding(user: ServerDoctorResponse | null): boolean {
  return !!user && !user.practiceId;
}
