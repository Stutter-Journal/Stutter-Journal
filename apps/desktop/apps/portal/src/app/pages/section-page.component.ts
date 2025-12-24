import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs';

@Component({
  selector: 'portal-section-page',
  standalone: true,
  imports: [AsyncPipe],
  template: `
    <section class="space-y-4">
      <h1 class="text-2xl font-semibold text-neutral-900">
        {{ title$ | async }}
      </h1>
      <p class="text-sm text-neutral-600">
        This area is ready for data views, patient workflows, and reporting
        surfaces.
      </p>
      <div class="grid gap-4 sm:grid-cols-2">
        <div class="rounded-2xl border border-neutral-200/70 bg-neutral-50 p-4">
          <p
            class="text-xs font-semibold uppercase tracking-[0.2em] text-neutral-500"
          >
            Next step
          </p>
          <p class="mt-2 text-sm text-neutral-700">
            Connect this section to live portal data.
          </p>
        </div>
        <div class="rounded-2xl border border-neutral-200/70 bg-neutral-50 p-4">
          <p
            class="text-xs font-semibold uppercase tracking-[0.2em] text-neutral-500"
          >
            Team note
          </p>
          <p class="mt-2 text-sm text-neutral-700">
            Use this layout to compose dashboards and intake tasks.
          </p>
        </div>
      </div>
    </section>
  `,
})
export class SectionPageComponent {
  private readonly route = inject(ActivatedRoute);

  readonly title$ = this.route.data.pipe(
    map((data) => (data['title'] as string) ?? 'Section'),
  );
}
