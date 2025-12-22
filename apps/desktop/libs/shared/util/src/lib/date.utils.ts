export type DateInput = Date | string | number;

export const toIsoDate = (input: DateInput): string => {
  const date = new Date(input);
  if (Number.isNaN(date.getTime())) {
    throw new Error('Invalid date input');
  }
  return date.toISOString();
};

export const startOfDayIso = (input: DateInput): string => {
  const date = new Date(input);
  if (Number.isNaN(date.getTime())) {
    throw new Error('Invalid date input');
  }
  date.setUTCHours(0, 0, 0, 0);
  return date.toISOString();
};

export const endOfDayIso = (input: DateInput): string => {
  const date = new Date(input);
  if (Number.isNaN(date.getTime())) {
    throw new Error('Invalid date input');
  }
  date.setUTCHours(23, 59, 59, 999);
  return date.toISOString();
};

export const formatShortDate = (
  input: DateInput,
  locale = 'en-US'
): string => {
  const date = new Date(input);
  if (Number.isNaN(date.getTime())) {
    throw new Error('Invalid date input');
  }
  return new Intl.DateTimeFormat(locale, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(date);
};
