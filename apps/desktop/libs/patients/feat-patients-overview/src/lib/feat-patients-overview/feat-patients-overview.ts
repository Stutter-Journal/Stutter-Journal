import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HlmDialogService } from '@spartan-ng/helm/dialog';
import {
  ServerLinkDTO,
  ServerPatientDTO,
  ServerPatientsResponse,
} from '@org/contracts';
import { PatientsClientService } from '@org/patients-data-access';

import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideMoreHorizontal,
  lucideUserPlus,
  lucideUsers,
} from '@ng-icons/lucide';

import { HlmButton } from '@spartan-ng/helm/button';
import { HlmInput } from '@spartan-ng/helm/input';
import {
  HlmCaption,
  HlmTable,
  HlmTableContainer,
  HlmTBody,
  HlmTd,
  HlmTh,
  HlmTHead,
  HlmTr,
} from '@spartan-ng/helm/table';
import {
  HlmDropdownMenu,
  HlmDropdownMenuItem,
  HlmDropdownMenuSeparator,
  HlmDropdownMenuTrigger,
} from '@spartan-ng/helm/dropdown-menu';
import { HlmBadge } from '@spartan-ng/helm/badge';
import {
  HlmEmpty,
  HlmEmptyDescription,
  HlmEmptyHeader,
  HlmEmptyMedia,
  HlmEmptyTitle,
} from '@spartan-ng/helm/empty';
import { HlmNumberedPagination } from '@spartan-ng/helm/pagination';
import { HlmIcon } from '@spartan-ng/helm/icon';
import { HlmItemFooter } from '@spartan-ng/helm/item';
import { AddPatient } from '../add-patient/add-patient';
import { take } from 'rxjs';

type PatientStatus = 'active' | 'pending' | 'archived';

interface Patient {
  id: string;
  fullName: string;
  dob?: string; // ISO date (yyyy-mm-dd)
  status: PatientStatus;
  lastEntryAt?: string; // ISO datetime
}

@Component({
  selector: 'lib-feat-patients-overview',
  imports: [
    CommonModule,
    HlmNumberedPagination,
    HlmDropdownMenuSeparator,
    RouterLink,
    HlmDropdownMenuItem,
    HlmIcon,
    HlmButton,
    NgIcon,
    HlmIcon,
    HlmInput,
    HlmDropdownMenuTrigger,
    HlmDropdownMenu,
    HlmDropdownMenuItem,
    HlmEmptyHeader,
    HlmEmpty,
    HlmEmptyMedia,
    HlmEmptyTitle,
    HlmEmptyDescription,
    HlmItemFooter,
    HlmTableContainer,
    HlmTable,
    HlmCaption,
    HlmTHead,
    HlmTr,
    HlmTh,
    HlmTBody,
    HlmTd,
    HlmBadge,
    HlmDropdownMenuSeparator,
  ],
  templateUrl: './feat-patients-overview.html',
  providers: [
    provideIcons({ lucideMoreHorizontal, lucideUserPlus, lucideUsers }),
  ],
})
export class FeatPatientsOverview implements OnInit {
  private readonly dialog = inject(HlmDialogService);
  private readonly patientsClient = inject(PatientsClientService);

  private readonly patientsSig = signal<Patient[]>([]);
  private lastRequestId = 0;

  readonly loading = this.patientsClient.loading;
  readonly error = this.patientsClient.error;

  // UI state
  readonly query = signal('');
  readonly statusFilter = signal<PatientStatus | 'all'>('all');

  readonly page = signal(1);
  readonly pageSize = signal(10);

  readonly filteredPatients = computed(() => {
    const sf = this.statusFilter();
    return this.patientsSig().filter((p) =>
      sf === 'all' ? true : p.status === sf,
    );
  });

  readonly totalItems = computed(() => this.filteredPatients().length);

  readonly totalPages = computed(() => {
    const size = Math.max(1, this.pageSize());
    return Math.max(1, Math.ceil(this.totalItems() / size));
  });

  readonly pagePatients = computed(() => {
    const size = Math.max(1, this.pageSize());
    const safePage = Math.min(Math.max(1, this.page()), this.totalPages());
    const start = (safePage - 1) * size;
    return this.filteredPatients().slice(start, start + size);
  });

  openAddPatient() {
    const ref = this.dialog.open(AddPatient, {
      contentClass: 'sm:!max-w-lg',
    });

    // Refresh the list after the dialog closes (pairing-code flow may have changed it).
    ref.closed$
      .pipe(take(1))
      .subscribe(() => void this.refreshPatients());
  }

  async ngOnInit(): Promise<void> {
    await this.refreshPatients();
  }

  private mapResponseToPatients(response: ServerPatientsResponse): Patient[] {
    const approved = (response.patients ?? []).map((p, idx) =>
      this.mapApprovedPatient(p, idx),
    );

    const pending = (response.pendingLinks ?? []).map((l, idx) =>
      this.mapPendingLink(l, idx),
    );

    return [...approved, ...pending];
  }

  private mapApprovedPatient(dto: ServerPatientDTO, index: number): Patient {
    const rawStatus = (dto as { status?: string }).status?.toLowerCase();
    const status: PatientStatus = rawStatus?.includes('inactive')
      ? 'archived'
      : 'active';

    const fullName =
      dto.displayName?.trim() || dto.email?.trim() || dto.id || `Patient ${index + 1}`;

    // Contracts currently don't include `birthDate/lastEntryAt`, but the backend might.
    const anyDto = dto as unknown as {
      birthDate?: string;
      dob?: string;
      lastEntryAt?: string;
    };

    return {
      id: dto.id ?? `patient_${index}`,
      fullName,
      dob: anyDto.birthDate ?? anyDto.dob,
      status,
      lastEntryAt: anyDto.lastEntryAt,
    };
  }

  private mapPendingLink(dto: ServerLinkDTO, index: number): Patient {
    const label = dto.patientId ? `Pending (${dto.patientId})` : 'Pending patient';
    return {
      id: dto.id ?? dto.patientId ?? `pending_${index}`,
      fullName: label,
      status: 'pending',
    };
  }

  private async refreshPatients(): Promise<void> {
    const requestId = ++this.lastRequestId;

    try {
      const response = await this.patientsClient.getPatientsResponse({
        search: this.query().trim(),
      });

      // Prevent out-of-order responses from clobbering newer results.
      if (requestId !== this.lastRequestId) return;

      this.patientsSig.set(this.mapResponseToPatients(response));
    } catch {
      // Error state is already handled by PatientsClientService; keep old list.
      if (requestId !== this.lastRequestId) return;
    }
  }

  onQueryInput(value: string) {
    this.query.set(value);
    this.page.set(1);
    void this.refreshPatients();
  }

  setStatusFilter(value: PatientStatus | 'all') {
    this.statusFilter.set(value);
    this.page.set(1);
  }

  // Simple formatting helpers
  fmtDate(iso?: string) {
    if (!iso) return '—';
    // For YYYY-MM-DD treat as local date, for datetime let Date parse.
    const d = iso.length === 10 ? new Date(`${iso}T00:00:00`) : new Date(iso);
    if (Number.isNaN(d.getTime())) return '—';
    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
    }).format(d);
  }

  badgeVariant(
    status: PatientStatus,
  ): 'default' | 'secondary' | 'outline' | 'destructive' {
    switch (status) {
      case 'active':
        return 'default';
      case 'pending':
        return 'secondary';
      case 'archived':
        return 'outline';
    }
  }

  statusLabel(status: PatientStatus) {
    switch (status) {
      case 'active':
        return 'Active';
      case 'pending':
        return 'Pending';
      case 'archived':
        return 'Archived';
    }
  }
}
