import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';

import { EntriesClientService } from '@org/data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { ServerEntryDTO, ServerPatientRowDTO } from '@org/contracts';
import { ErrorResponse, normalizeError } from '@org/util';

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
  HlmEmpty,
  HlmEmptyDescription,
  HlmEmptyHeader,
  HlmEmptyTitle,
} from '@spartan-ng/helm/empty';
import { HlmItemFooter } from '@spartan-ng/helm/item';
import { HlmBadge } from '@spartan-ng/helm/badge';
import { HlmNumberedPagination } from '@spartan-ng/helm/pagination';

type LinkStatus = 'Approved' | 'Pending' | 'Revoked' | 'Denied';

interface PatientRef {
  id: string;
  name: string;
}

interface EntryVm {
  id: string;
  patientId: string;
  patientName: string;
  happenedAt?: string;
  notes?: string;
  notesPreview: string;
  tags: string[];
  stutterFrequency?: number;
}

@Component({
  selector: 'lib-feat-entry-list',
  imports: [
    CommonModule,
    HlmButton,
    HlmInput,
    HlmTableContainer,
    HlmTable,
    HlmCaption,
    HlmTHead,
    HlmTr,
    HlmTh,
    HlmTBody,
    HlmTd,
    HlmEmptyHeader,
    HlmEmpty,
    HlmEmptyTitle,
    HlmEmptyDescription,
    HlmItemFooter,
    HlmBadge,
    HlmNumberedPagination,
  ],
  templateUrl: './feat-entry-list.html',
})
export class FeatEntryList implements OnInit {
  private readonly entriesApi = inject(EntriesClientService);
  private readonly patientsApi = inject(PatientsClientService);

  private readonly entriesSig = signal<EntryVm[]>([]);
  private readonly loadingSig = signal(false);
  private readonly errorSig = signal<ErrorResponse | null>(null);

  readonly loading = computed(() => this.loadingSig());
  readonly error = computed(() => this.errorSig());

  readonly query = signal('');
  readonly page = signal(1);
  readonly pageSize = signal(10);

  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    const rows = this.entriesSig();
    if (!q) return rows;

    return rows.filter((row) => {
      const tags = row.tags.join(' ').toLowerCase();
      return (
        row.patientName.toLowerCase().includes(q) ||
        (row.notes ?? '').toLowerCase().includes(q) ||
        tags.includes(q)
      );
    });
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

  onQueryInput(value: string) {
    this.query.set(value);
    this.page.set(1);
  }

  async refresh(): Promise<void> {
    this.loadingSig.set(true);
    this.errorSig.set(null);

    try {
      const patientsResponse = await this.patientsApi.getPatientsResponse();
      const patients = this.buildApprovedPatients(patientsResponse.rows ?? []);
      if (patients.length === 0) {
        this.entriesSig.set([]);
        return;
      }

      const results = await Promise.allSettled(
        patients.map((patient) => this.entriesApi.getEntries(patient.id)),
      );

      const entries: EntryVm[] = [];
      const failures: unknown[] = [];

      results.forEach((result, index) => {
        const patient = patients[index];
        if (result.status === 'fulfilled') {
          result.value.forEach((entry, entryIndex) => {
            entries.push(this.mapEntry(entry, patient, entryIndex));
          });
        } else {
          failures.push(result.reason);
        }
      });

      entries.sort(
        (a, b) =>
          this.entryTimestamp(b.happenedAt) -
          this.entryTimestamp(a.happenedAt),
      );
      this.entriesSig.set(entries);

      if (failures.length > 0) {
        const normalized = this.toErrorResponse(failures[0]);
        this.errorSig.set({
          ...normalized,
          message:
            failures.length > 1
              ? 'Some entries could not be loaded. Try refreshing.'
              : normalized.message,
        });
      }
    } catch (err) {
      this.entriesSig.set([]);
      this.errorSig.set(this.toErrorResponse(err));
    } finally {
      this.loadingSig.set(false);
    }
  }

  fmtDateTime(iso?: string) {
    if (!iso) return '-';
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return '-';
    return new Intl.DateTimeFormat(undefined, {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    }).format(d);
  }

  fmtStutterFrequency(value?: number) {
    return typeof value === 'number' ? `${value}` : '-';
  }

  private buildApprovedPatients(rows: ServerPatientRowDTO[]): PatientRef[] {
    return rows
      .map((row, index) => {
        const patient = row.patient;
        const link = row.link;
        if (!patient?.id) return null;
        const status = (link?.status ?? '').toString().toLowerCase();
        if (status !== 'approved') {
          return null;
        }

        const name =
          patient.displayName?.trim() ||
          patient.email?.trim() ||
          patient.id ||
          `Patient ${index + 1}`;

        return { id: patient.id, name };
      })
      .filter((row): row is PatientRef => row !== null);
  }

  private mapEntry(
    entry: ServerEntryDTO,
    patient: PatientRef,
    index: number,
  ): EntryVm {
    const tags = Array.isArray(entry.tags)
      ? entry.tags.filter((tag): tag is string => Boolean(tag))
      : [];

    return {
      id: entry.id ?? `${patient.id}_${index}`,
      patientId: patient.id,
      patientName: patient.name,
      happenedAt: entry.happenedAt ?? undefined,
      notes: entry.notes ?? undefined,
      notesPreview: this.formatNotes(entry.notes),
      tags,
      stutterFrequency: entry.stutterFrequency ?? undefined,
    };
  }

  private formatNotes(notes?: string): string {
    if (!notes) return '-';
    const cleaned = notes.replace(/\s+/g, ' ').trim();
    if (!cleaned) return '-';
    const limit = 160;
    if (cleaned.length <= limit) return cleaned;
    return `${cleaned.slice(0, limit).trim()}...`;
  }

  private entryTimestamp(iso?: string): number {
    if (!iso) return 0;
    const ts = Date.parse(iso);
    return Number.isNaN(ts) ? 0 : ts;
  }

  private toErrorResponse(err: unknown): ErrorResponse {
    if (err && typeof err === 'object') {
      const maybe = err as {
        status?: unknown;
        message?: unknown;
        code?: unknown;
        details?: unknown;
      };
      if (typeof maybe.message === 'string' && typeof maybe.status === 'number') {
        return {
          status: maybe.status,
          message: maybe.message,
          code: typeof maybe.code === 'string' ? maybe.code : undefined,
          details: maybe.details,
        };
      }
    }

    return normalizeError(err);
  }
}
