import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HlmButton } from '@spartan-ng/helm/button';
import {
  lucideLayoutDashboard,
  lucidePersonStanding,
  lucideNotebookPen,
  lucideBarChart,
  lucideSettings,
} from '@ng-icons/lucide';
import {
  HlmSidebar,
  HlmSidebarContent,
  HlmSidebarGroup,
  HlmSidebarHeader,
  HlmSidebarTrigger,
  HlmSidebarWrapper,
} from '@spartan-ng/helm/sidebar';
import { HlmMenubar, HlmMenubarTrigger } from '@spartan-ng/helm/menubar';
import {
  HlmDropdownMenu,
  HlmDropdownMenuItem,
  HlmDropdownMenuSeparator,
} from '@spartan-ng/helm/dropdown-menu';
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
import { AuthClientService } from '@org/auth-data-access';
import { Logout } from '../logout/logout';
import { HlmIcon } from '@spartan-ng/helm/icon';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { HlmDialogService } from '@spartan-ng/helm/dialog';
import { AddPatient } from '@org/feat-patients-overview';
import { EntriesClientService, RecentEntriesRow } from '@org/data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { ServerEntryDTO, ServerPatientRowDTO } from '@org/contracts';
import { ErrorResponse, normalizeError } from '@org/util';

@Component({
  selector: 'lib-dashboard',
  imports: [
    HlmSidebarWrapper,
    HlmSidebar,
    HlmSidebarHeader,
    HlmSidebarContent,
    HlmSidebarGroup,
    CommonModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    HlmButton,
    HlmSidebarTrigger,
    HlmMenubar,
    HlmMenubarTrigger,
    HlmDropdownMenu,
    HlmDropdownMenuItem,
    HlmDropdownMenuSeparator,
    HlmTableContainer,
    HlmTable,
    HlmCaption,
    HlmTHead,
    HlmTr,
    HlmTh,
    HlmTBody,
    HlmTd,
    Logout,
    HlmIcon,
    NgIcon,
  ],
  templateUrl: './dashboard.html',
  providers: [
    provideIcons({
      lucideLayoutDashboard,
      lucidePersonStanding,
      lucideNotebookPen,
      lucideBarChart,
      lucideSettings,
    }),
  ],
})
export class Dashboard implements OnInit {
  readonly auth = inject(AuthClientService);
  private readonly dialog = inject(HlmDialogService);
  private readonly entries = inject(EntriesClientService);
  private readonly patients = inject(PatientsClientService);

  readonly patientsCount = signal<number | null>(null);
  readonly entriesCountRecent = signal<number | null>(null);
  readonly pendingLinksCount = signal<number | null>(null);

  readonly recentLoading = signal(false);
  readonly recentError = signal<string | null>(null);
  readonly recentRows = signal<RecentActivityRow[]>([]);

  async ngOnInit(): Promise<void> {
    await Promise.all([this.loadRecentActivity(), this.loadKpis()]);
  }

  openAddPatient(): void {
    this.dialog.open(AddPatient, { contentClass: 'sm:!max-w-lg' });
  }

  private async loadKpis(): Promise<void> {
    try {
      const patientsResp = await this.patients.getPatientsResponse();
      const summary = this.summarizePatients(patientsResp.rows ?? []);

      this.patientsCount.set(summary.approvedPatients.length);
      this.pendingLinksCount.set(summary.pendingCount);
    } catch (err) {
      this.patientsCount.set(null);
      this.pendingLinksCount.set(null);
      this.entriesCountRecent.set(null);
    }
  }

  private async loadRecentActivity(): Promise<void> {
    this.recentLoading.set(true);
    this.recentError.set(null);
    try {
      const recent = await this.entries.getRecentEntries(10);
      const rows = (recent.rows ?? [])
        .map((row, index) => this.toRecentRowFromRecent(row, index))
        .filter((row): row is RecentActivityRow => row !== null)
        .sort((a, b) => b.timestamp - a.timestamp)
        .slice(0, 5);

      if (rows.length === 0) {
        await this.loadRecentFallback();
      } else {
        this.recentRows.set(rows);
        this.entriesCountRecent.set(rows.length);
      }
    } catch (err) {
      await this.loadRecentFallback();

      if (this.recentRows().length > 0) {
        this.recentError.set(null);
      } else {
        const normalized = this.toErrorResponse(err);
        this.recentError.set(normalized.message || 'Could not load entries');
      }
    } finally {
      this.recentLoading.set(false);
    }
  }

  private async loadRecentFallback(): Promise<void> {
    try {
      const patientsResp = await this.patients.getPatientsResponse();
      const summary = this.summarizePatients(patientsResp.rows ?? []);
      const patients = summary.approvedPatients;
      if (patients.length === 0) {
        this.recentRows.set([]);
        this.entriesCountRecent.set(0);
        return;
      }

      const results = await Promise.allSettled(
        patients.map((patient) => this.entries.getEntries(patient.id)),
      );

      const entries: RecentActivityRow[] = [];
      const failures: unknown[] = [];

      results.forEach((result, index) => {
        const patient = patients[index];
        if (result.status === 'fulfilled') {
          result.value.forEach((entry, entryIndex) => {
            const row = this.toRecentRow(entry, patient, entryIndex);
            if (row) entries.push(row);
          });
        } else {
          failures.push(result.reason);
        }
      });

      entries.sort((a, b) => b.timestamp - a.timestamp);
      const recent = entries.slice(0, 5);
      this.recentRows.set(recent);
      this.entriesCountRecent.set(recent.length);

      if (failures.length > 0 && this.recentRows().length === 0) {
        const normalized = this.toErrorResponse(failures[0]);
        this.recentError.set(
          failures.length > 1
            ? 'Some entries could not be loaded. Try refreshing.'
            : normalized.message,
        );
      }
    } catch (err) {
      const normalized = this.toErrorResponse(err);
      this.recentError.set(normalized.message || 'Could not load entries');
    }
  }

  private toRecentRow(
    entry: ServerEntryDTO,
    patient: PatientRef,
    index: number,
  ): RecentActivityRow | null {
    const ts = this.entryTimestamp(entry.happenedAt ?? entry.createdAt);
    if (!Number.isFinite(ts) || ts === 0) return null;

    const tag = entry.tags?.[0]?.trim();

    return {
      id: entry.id ?? `${patient.id}_${index}`,
      patientId: patient.id,
      patient: patient.name,
      timestamp: ts,
      tag: tag && tag.length > 0 ? tag : '—',
      stutterFreq:
        typeof entry.stutterFrequency === 'number'
          ? entry.stutterFrequency
          : null,
    };
  }

  private toRecentRowFromRecent(
    row: RecentEntriesRow,
    index: number,
  ): RecentActivityRow | null {
    const entry = row.entry;
    const patient = row.patient;
    if (!entry || !patient?.id) return null;

    const name =
      patient.displayName?.trim() ||
      patient.email?.trim() ||
      patient.id ||
      `Patient ${index + 1}`;

    return this.toRecentRow(entry, { id: patient.id, name }, index);
  }

  private summarizePatients(rows: ServerPatientRowDTO[]): PatientSummary {
    const approvedPatients: PatientRef[] = [];
    let pendingCount = 0;

    rows.forEach((row, index) => {
      const patient = row.patient;
      const status = (row.link?.status ?? '').toString().toLowerCase();

      if (status === 'pending') {
        pendingCount += 1;
      }

      if (!patient?.id || status !== 'approved') return;

      const name =
        patient.displayName?.trim() ||
        patient.email?.trim() ||
        patient.id ||
        `Patient ${index + 1}`;

      approvedPatients.push({ id: patient.id, name });
    });

    return { approvedPatients, pendingCount };
  }

  private entryTimestamp(iso?: string | null): number {
    if (!iso) return 0;
    const ts = Date.parse(iso);
    return Number.isNaN(ts) ? 0 : ts;
  }

  private toErrorResponse(err: unknown): ErrorResponse {
    return normalizeError(err);
  }

  fmtDate(ts: number): string {
    const d = new Date(ts);
    if (!Number.isFinite(d.getTime())) return '—';
    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
    }).format(d);
  }
}

interface RecentActivityRow {
  id: string;
  patientId: string;
  patient: string;
  timestamp: number;
  tag: string;
  stutterFreq: number | null;
}

interface PatientRef {
  id: string;
  name: string;
}

interface PatientSummary {
  approvedPatients: PatientRef[];
  pendingCount: number;
}
