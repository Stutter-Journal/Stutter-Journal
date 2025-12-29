import { AsyncPipe, NgIf } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { DoctorContextService } from '../doctor/doctor-context.service';

@Component({
  selector: 'portal-app-shell',
  standalone: true,
  imports: [AsyncPipe, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  private readonly doctorContext = inject(DoctorContextService);

  readonly showOnboarding$ = this.doctorContext.doctor$.pipe(
    map((doctor) => !doctor?.practiceId),
  );
}
