import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';

import { HlmDialogService } from '@spartan-ng/helm/dialog';
import { PatientsClientService } from '@org/patients-data-access';
import { ServerPatientRowDTO, ServerPatientsResponse } from '@org/contracts';

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

type LinkStatus = 'Approved' | 'Pending' | 'Revoked' | 'Denied';

// What the overview UI actually cares about
type PatientStatus = 'active' | 'pending' | 'revoked' | 'denied';

interface PatientVm {
  patientId: string;
  fullName: string;
  email?: string;
  patientCode?: string;

  linkId: string;
  linkStatus: LinkStatus;
  status: PatientStatus;

  requestedAt?: string; // ISO
  approvedAt?: string; // ISO
}

@Component({
  selector: 'lib-feat-patients-overview',
  imports: [
    CommonModule,
    RouterLink,

    // UI modules you already had
    HlmNumberedPagination,
    HlmDropdownMenuSeparator,
    HlmDropdownMenuItem,
    HlmIcon,
    HlmButton,
    NgIcon,
    HlmInput,
    HlmDropdownMenuTrigger,
    HlmDropdownMenu,
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
  ],
  templateUrl: './feat-patients-overview.html',
  providers: [
    provideIcons({ lucideMoreHorizontal, lucideUserPlus, lucideUsers }),
  ],
})
export class FeatPatientsOverview implements OnInit {
  private readonly dialog = inject(HlmDialogService);
  private readonly api = inject(PatientsClientService);

  readonly loading = this.api.loading;
  readonly error = this.api.error;

  // UI state
  readonly query = signal('');
  readonly statusFilter = signal<PatientStatus | 'all'>('all');
  readonly page = signal(1);
  readonly pageSize = signal(10);

  private readonly rowsSig = signal<PatientVm[]>([]);

  readonly filtered = computed(() => {
    const sf = this.statusFilter();
    return this.rowsSig().filter((r) =>
      sf === 'all' ? true : r.status === sf,
    );
  });

  readonly totalItems = computed(() => this.filtered().length);

  readonly totalPages = computed(() => {
    const size = Math.max(1, this.pageSize());
    return Math.max(1, Math.ceil(this.totalItems() / size));
  });

  readonly pageRows = computed(() => {
    const size = Math.max(1, this.pageSize());
    const safePage = Math.min(Math.max(1, this.page()), this.totalPages());
    const start = (safePage - 1) * size;
    return this.filtered().slice(start, start + size);
  });

  async ngOnInit(): Promise<void> {
    await this.refresh();
  }

  openAddPatient() {
    const ref = this.dialog.open(AddPatient, { contentClass: 'sm:!max-w-lg' });
    ref.closed$.pipe(take(1)).subscribe(() => void this.refresh());
  }

  onQueryInput(value: string) {
    this.query.set(value);
    this.page.set(1);
    void this.refresh();
  }

  setStatusFilter(value: PatientStatus | 'all') {
    this.statusFilter.set(value);
    this.page.set(1);
  }

  private async refresh(): Promise<void> {
    // service now supports search, and BFF forwards it ✅
    const res = await this.api.getPatientsResponse({
      search: this.query().trim(),
    });
    this.rowsSig.set(this.mapResponse(res));
  }

  private mapResponse(res: ServerPatientsResponse): PatientVm[] {
    return (res.rows ?? [])
      .map((row, idx) => this.mapRow(row, idx))
      .filter((x): x is PatientVm => x !== null);
  }

  private mapRow(row: ServerPatientRowDTO, index: number): PatientVm | null {
    const p = row.patient;
    const l = row.link;
    if (!p || !l) return null;

    const fullName =
      p.displayName?.trim() ||
      p.email?.trim() ||
      p.id ||
      `Patient ${index + 1}`;

    const linkStatus = (l.status ?? 'Pending') as LinkStatus;

    return {
      patientId: p.id ?? `patient_${index}`,
      fullName,
      email: p.email ?? undefined,
      patientCode: p.patientCode ?? undefined,

      linkId: l.id ?? `link_${index}`,
      linkStatus,
      status: this.toPatientStatus(linkStatus),

      requestedAt: l.requestedAt ?? undefined,
      approvedAt: l.approvedAt ?? undefined,
    };
  }

  private toPatientStatus(s: LinkStatus): PatientStatus {
    switch (s) {
      case 'Approved':
        return 'active';
      case 'Pending':
        return 'pending';
      case 'Revoked':
        return 'revoked';
      case 'Denied':
        return 'denied';
    }
  }

  // formatting helpers
  fmtDate(iso?: string) {
    if (!iso) return '—';
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
      case 'revoked':
        return 'destructive';
      case 'denied':
        return 'outline';
    }
  }

  statusLabel(status: PatientStatus) {
    switch (status) {
      case 'active':
        return 'Connected';
      case 'pending':
        return 'Pending';
      case 'revoked':
        return 'Revoked';
      case 'denied':
        return 'Denied';
    }
  }

  readonly statusFilterLabel = computed(() => {
    const s = this.statusFilter();
    return s === 'all' ? 'All' : this.statusLabel(s);
  });
}
