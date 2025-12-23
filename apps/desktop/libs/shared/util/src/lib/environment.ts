export interface AppEnvironment {
  useMocks: boolean;
}

export const environment: AppEnvironment = {
  useMocks: false,
};

const normalizeFlag = (value: unknown): boolean => {
  return value === true || value === 'true' || value === '1';
};

export const resolveUseMocksFlag = (): boolean => {
  const globalFlag =
    typeof globalThis !== 'undefined'
      ? (globalThis as { __USE_MOCKS__?: unknown }).__USE_MOCKS__
      : undefined;

  let processFlag: unknown;
  if (typeof process !== 'undefined' && typeof process.env !== 'undefined') {
    processFlag = process.env['USE_MOCKS'] ?? process.env['NX_USE_MOCKS'];
  }

  const importMetaFlag =
    typeof import.meta !== 'undefined'
      ? (import.meta as { env?: Record<string, unknown> }).env?.['USE_MOCKS'] ??
        (import.meta as { env?: Record<string, unknown> }).env?.['NX_USE_MOCKS']
      : undefined;

  let storedFlag: string | null = null;
  try {
    storedFlag =
      typeof localStorage !== 'undefined'
        ? localStorage.getItem('portal:useMocks')
        : null;
  } catch {
    // ignore
  }

  return [globalFlag, processFlag, importMetaFlag, storedFlag].some((value) =>
    normalizeFlag(value)
  );
};
