import { assign, createActor, createMachine, fromPromise } from 'xstate';

export type RequestFlowState = 'idle' | 'submitting' | 'success' | 'failure';

export interface RequestFlowSnapshot<TInput> {
  state: RequestFlowState;
  input?: TInput;
  error?: string;
}

export interface RequestFlow<TInput> {
  start(): void;
  stop(): void;
  submit(input: TInput): void;
  reset(): void;
  getSnapshot(): RequestFlowSnapshot<TInput>;
  subscribe(listener: (snapshot: RequestFlowSnapshot<TInput>) => void): () => void;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function defaultErrorMessage(error: unknown): string {
  if (typeof error === 'string') return error;
  if (isRecord(error) && typeof error['message'] === 'string') return error['message'];
  return 'Request failed';
}

export function createRequestFlow<TInput>(options: {
  request: (input: TInput) => Promise<unknown>;
  errorMessage?: (error: unknown) => string;
}): RequestFlow<TInput> {
  const errorMessage = options.errorMessage ?? defaultErrorMessage;

  type Ctx = { input?: TInput; error?: string };
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
        entry: assign(() => ({ error: undefined })),
        on: {
          SUBMIT: {
            target: 'submitting',
            actions: assign(({ event }) => ({ input: event.input, error: undefined })),
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
          },
          onError: {
            target: 'failure',
            actions: assign(({ event }) => ({
              error: errorMessage((event as unknown as { error?: unknown }).error),
            })),
          },
        },
      },
      success: {
        on: {
          RESET: { target: 'idle' },
          SUBMIT: {
            target: 'submitting',
            actions: assign(({ event }) => ({ input: event.input, error: undefined })),
          },
        },
      },
      failure: {
        on: {
          RESET: { target: 'idle', actions: assign(() => ({ error: undefined })) },
          RETRY: { target: 'submitting' },
          SUBMIT: {
            target: 'submitting',
            actions: assign(({ event }) => ({ input: event.input, error: undefined })),
          },
        },
      },
    },
  });

  const actor = createActor(machine);

  const listeners = new Set<(snapshot: RequestFlowSnapshot<TInput>) => void>();

  function emit() {
    const snapshot = getSnapshot();
    for (const listener of listeners) listener(snapshot);
  }

  const subscription = actor.subscribe(() => emit());

  function getSnapshot(): RequestFlowSnapshot<TInput> {
    const s = actor.getSnapshot();
    const value = s.value;
    const state =
      value === 'idle' || value === 'submitting' || value === 'success' || value === 'failure'
        ? value
        : 'idle';

    return {
      state,
      input: s.context.input,
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
