import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Doctor, Practice } from '@org/models';

export type DoctorContextState = Partial<Doctor> | null;

@Injectable({ providedIn: 'root' })
export class DoctorContextService {
  private readonly doctorSubject = new BehaviorSubject<DoctorContextState>(
    null,
  );

  readonly doctor$ = this.doctorSubject.asObservable();

  setDoctor(doctor: Doctor): void {
    this.doctorSubject.next(doctor);
  }

  setPractice(practice: Practice): void {
    const current = this.doctorSubject.value ?? {};
    this.doctorSubject.next({ ...current, practiceId: practice.id });
  }

  clear(): void {
    this.doctorSubject.next(null);
  }
}
