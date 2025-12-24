import { Id } from '../types';

export interface Practice {
  id: Id;
  name: string;
  address?: string | null;
  logoUrl?: string | null;
}
