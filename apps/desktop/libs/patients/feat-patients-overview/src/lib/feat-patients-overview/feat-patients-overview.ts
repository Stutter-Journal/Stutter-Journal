import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

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
  styleUrl: './feat-patients-overview.css',
  providers: [
    provideIcons({ lucideMoreHorizontal, lucideUserPlus, lucideUsers }),
  ],
})
export class FeatPatientsOverview {
  // Replace this with your API/store later.
  private readonly _patients = signal<Patient[]>([
    {
      id: 'p_001',
      fullName: 'Anna Müller',
      dob: '1994-03-12',
      status: 'active',
      lastEntryAt: '2026-01-05T10:15:00Z',
    },
    {
      id: 'p_002',
      fullName: 'Lukas Steiner',
      dob: '2016-09-21',
      status: 'pending',
    },
    {
      id: 'p_003',
      fullName: 'Mina Novak',
      status: 'archived',
      lastEntryAt: '2025-12-12T08:00:00Z',
    },
  ]);

  // UI state
  readonly query = signal('');
  readonly statusFilter = signal<PatientStatus | 'all'>('all');

  readonly page = signal(1);
  readonly pageSize = signal(10);

  readonly filteredPatients = computed(() => {
    const q = this.query().trim().toLowerCase();
    const sf = this.statusFilter();

    return this._patients()
      .filter((p) => (sf === 'all' ? true : p.status === sf))
      .filter((p) => (q ? p.fullName.toLowerCase().includes(q) : true));
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

  onQueryInput(value: string) {
    this.query.set(value);
    this.page.set(1);
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
