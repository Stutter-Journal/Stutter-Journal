export type AsyncSequenceItem<T> = T | Error | (() => T | Promise<T>);

export function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function createAsyncSequence<T>(
  items: AsyncSequenceItem<T>[],
  options?: { repeatLast?: boolean; fallback?: AsyncSequenceItem<T> },
): () => Promise<T> {
  const repeatLast = options?.repeatLast ?? true;
  const fallback: AsyncSequenceItem<T> | undefined = options?.fallback;
  let index = 0;

  return async () => {
    const hasItem = index < items.length;
    const item = hasItem
      ? items[index]
      : repeatLast
        ? items[Math.max(0, items.length - 1)]
        : fallback;

    index += 1;

    if (item == null) {
      throw new Error('Async sequence exhausted');
    }

    if (item instanceof Error) {
      throw item;
    }

    if (typeof item === 'function') {
      return await (item as () => T | Promise<T>)();
    }

    return item;
  };
}
