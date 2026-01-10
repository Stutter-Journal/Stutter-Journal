import { assign, createActor, createMachine, fromPromise } from 'xstate';

export type RequestFlowState = 'idle' | 'submitting' | 'success' | 'failure';

export interface RequestFlowSnapshot<TInput, TResult = unknown> {
  state: RequestFlowState;
  input?: TInput;
  result?: TResult;
  error?: string;
}

export interface RequestFlow<TInput, TResult = unknown> {
  start(): void;
  stop(): void;
  submit(input: TInput): void;
  reset(): void;
  getSnapshot(): RequestFlowSnapshot<TInput, TResult>;
  subscribe(
    listener: (snapshot: RequestFlowSnapshot<TInput, TResult>) => void,
  ): () => void;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function defaultErrorMessage(error: unknown): string {
  if (typeof error === 'string') return error;
  if (isRecord(error) && typeof error['message'] === 'string')
    return error['message'];
  // Common normalized error shape in this repo: { error: { message: string, ... } }
  if (
    isRecord(error) &&
    isRecord(error['error']) &&
    typeof (error['error'] as Record<string, unknown>)['message'] === 'string'
  ) {
    return (error['error'] as Record<string, unknown>)['message'] as string;
  }
  return 'Request failed';
}

export function createRequestFlow<TInput, TResult = unknown>(options: {
  request: (input: TInput) => Promise<TResult>;
  errorMessage?: (error: unknown) => string;
}): RequestFlow<TInput, TResult> {
  const errorMessage = options.errorMessage ?? defaultErrorMessage;

  type Ctx = { input?: TInput; result?: TResult; error?: string };
  type Ev =
    | { type: 'SUBMIT'; input: TInput }
    | { type: 'RESET' }
    | { type: 'RETRY' };

  const requestLogic = fromPromise(async ({ input }: { input: TInput }) => {
    return options.request(input);
  });

  const machine = createMachine({
    id: 'requestFlow',
    types: {} as { context: Ctx; events: Ev },
    initial: 'idle',
    context: {},
    states: {
      idle: {
        entry: assign(() => ({ error: undefined, result: undefined })),
        on: {
          SUBMIT: {
            target: 'submitting',
            actions: assign(({ event }) => ({
              input: event.input,
              error: undefined,
              result: undefined,
            })),
          },
        },
      },
      submitting: {
        invoke: {
          src: requestLogic,
          input: ({ context }): TInput => {
            if (context.input === undefined) {
              throw new Error('Missing input');
            }
            return context.input;
          },
          onDone: {
            target: 'success',
            actions: assign(({ event }) => ({
              result: (event as unknown as { output?: TResult }).output,
            })),
          },
          onError: {
            target: 'failure',
            actions: assign(({ event }) => ({
              error: errorMessage(
                (event as unknown as { error?: unknown }).error,
              ),
            })),
          },
        },
      },
      success: {
        on: {
          RESET: { target: 'idle' },
          SUBMIT: {
            target: 'submitting',
            actions: assign(({ event }) => ({
              input: event.input,
              error: undefined,
              result: undefined,
            })),
          },
        },
      },
      failure: {
        on: {
          RESET: {
            target: 'idle',
            actions: assign(() => ({ error: undefined, result: undefined })),
          },
          RETRY: { target: 'submitting' },
          SUBMIT: {
            target: 'submitting',
            actions: assign(({ event }) => ({
              input: event.input,
              error: undefined,
              result: undefined,
            })),
          },
        },
      },
    },
  });

  const actor = createActor(machine);

  const listeners = new Set<
    (snapshot: RequestFlowSnapshot<TInput, TResult>) => void
  >();

  function emit() {
    const snapshot = getSnapshot();
    for (const listener of listeners) listener(snapshot);
  }

  const subscription = actor.subscribe(() => emit());

  function getSnapshot(): RequestFlowSnapshot<TInput, TResult> {
    const s = actor.getSnapshot();
    const value = s.value;
    const state =
      value === 'idle' ||
      value === 'submitting' ||
      value === 'success' ||
      value === 'failure'
        ? value
        : 'idle';

    return {
      state,
      input: s.context.input,
      result: s.context.result,
      error: s.context.error,
    };
  }

  return {
    start() {
      actor.start();
      emit();
    },
    stop() {
      subscription.unsubscribe();
      actor.stop();
      listeners.clear();
    },
    submit(input: TInput) {
      actor.send({ type: 'SUBMIT', input });
    },
    reset() {
      actor.send({ type: 'RESET' });
    },
    getSnapshot,
    subscribe(listener) {
      listeners.add(listener);
      listener(getSnapshot());
      return () => listeners.delete(listener);
    },
  };
}
