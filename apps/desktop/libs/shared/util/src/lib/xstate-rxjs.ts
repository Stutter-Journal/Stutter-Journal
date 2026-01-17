import type { AnyActorRef, SnapshotFrom } from 'xstate';
import { Observable } from 'rxjs';

/**
 * Turns an XState actor into an RxJS stream of snapshots.
 * Emits the current snapshot immediately, then on every transition.
 */
export function actorSnapshot$<TActor extends AnyActorRef>(
  actor: TActor,
): Observable<SnapshotFrom<TActor>> {
  return new Observable<SnapshotFrom<TActor>>((subscriber) => {
    subscriber.next(actor.getSnapshot());

    const sub = actor.subscribe((snapshot) => {
      subscriber.next(snapshot as SnapshotFrom<TActor>);
    });

    return () => sub.unsubscribe();
  });
}
