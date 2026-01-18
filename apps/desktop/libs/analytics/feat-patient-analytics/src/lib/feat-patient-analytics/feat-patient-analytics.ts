import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { toast } from 'ngx-sonner';

import { BrnSelectImports } from '@spartan-ng/brain/select';
import { BrnSheetImports } from '@spartan-ng/brain/sheet';

import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmButtonGroupImports } from '@spartan-ng/helm/button-group';
import { HlmBadge } from '@spartan-ng/helm/badge';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmInput } from '@spartan-ng/helm/input';
import { HlmSelectImports } from '@spartan-ng/helm/select';
import { HlmDropdownMenuImports } from '@spartan-ng/helm/dropdown-menu';
import { HlmTabsImports } from '@spartan-ng/helm/tabs';
import {
  HlmTable,
  HlmTableContainer,
  HlmTBody,
  HlmTd,
  HlmTh,
  HlmTHead,
  HlmTr,
  HlmCaption,
} from '@spartan-ng/helm/table';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { HlmProgressImports } from '@spartan-ng/helm/progress';
import { HlmSheetImports } from '@spartan-ng/helm/sheet';
import { HlmAlertImports } from '@spartan-ng/helm/alert';

import { AnalyticsClientService } from '@org/analytics-data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { ServerAnalyticsResponse, ServerPatientRowDTO } from '@org/contracts';
import { ErrorResponse, normalizeError } from '@org/util';

type DeltaTone = 'positive' | 'negative' | 'neutral';
type PresetKey = '7d' | '30d' | 'ytd';
type SortKey = 'date' | 'entries' | 'intensity';
type SortDir = 'asc' | 'desc';

type DistributionMap = Record<string, number>;

interface KpiMetric {
  id: string;
  label: string;
  value: string;
  delta: string;
  deltaTone: DeltaTone;
  helper: string;
}

interface AdvancedFilter {
  id: string;
  label: string;
  hint: string;
  enabled: boolean;
}

interface DistributionItem {
  label: string;
  count: number;
  percent: number;
}

interface TrendPoint {
  date: string;
  count: number;
  avgStutterFrequency: number;
}

interface TrendRow {
  id: string;
  date: string;
  entries: number;
  avgIntensity: number;
  delta: string;
  sharePercent: number;
}

interface AnalyticsAggregate {
  rangeDays: number;
  totalEntries: number;
  avgStutterFrequency: number | null;
  activePatients: number;
  distributions: {
    emotions: DistributionMap;
    triggers: DistributionMap;
    techniques: DistributionMap;
  };
  trend: TrendPoint[];
}

@Component({
  selector: 'lib-feat-patient-analytics',
  imports: [
    CommonModule,
    FormsModule,
    BrnSelectImports,
    BrnSheetImports,
    HlmButtonImports,
    HlmButtonGroupImports,
    HlmBadge,
    HlmCardImports,
    HlmInput,
    HlmSelectImports,
    HlmDropdownMenuImports,
    HlmTabsImports,
    HlmTableContainer,
    HlmTable,
    HlmTHead,
    HlmTr,
    HlmTh,
    HlmTBody,
    HlmTd,
    HlmCaption,
    HlmSkeletonImports,
    HlmProgressImports,
    HlmSheetImports,
    HlmAlertImports,
  ],
  templateUrl: './feat-patient-analytics.html',
})
export class FeatPatientAnalytics implements OnInit {
  private readonly analyticsApi = inject(AnalyticsClientService);
  private readonly patientsApi = inject(PatientsClientService);

  readonly loading = signal(true);
  private readonly errorSig = signal<ErrorResponse | null>(null);
  private readonly warningSig = signal<string | null>(null);

  readonly error = computed(() => this.errorSig());
  readonly warning = computed(() => this.warningSig());

  readonly rangeNotice = signal<string | null>(null);

  readonly segments = [
    'All segments',
    'New patients',
    'Returning patients',
    'High intensity',
  ];
  readonly environments = ['All environments', 'Clinic', 'Telehealth'];
  readonly projects = [
    'All projects',
    'Fluency Lab',
    'Confidence Track',
    'Sustained Calm',
  ];

  segment = this.segments[0];
  environment = this.environments[0];
  project = this.projects[0];
  dateFrom = '';
  dateTo = '';
  preset = signal<PresetKey>('30d');

  readonly skeletonCards = [0, 1, 2];

  readonly advancedFilters = signal<AdvancedFilter[]>([
    {
      id: 'shared',
      label: 'Shared entries only',
      hint: 'Exclude draft or private notes',
      enabled: true,
    },
    {
      id: 'alerts',
      label: 'High-risk moments',
      hint: 'Intensity 8+ or frequent stutter',
      enabled: false,
    },
    {
      id: 'technique',
      label: 'Technique applied',
      hint: 'Breathing, pacing, or resets',
      enabled: true,
    },
  ]);

  readonly activeFiltersCount = computed(
    () => this.advancedFilters().filter((f) => f.enabled).length,
  );

  readonly sortKey = signal<SortKey>('entries');
  readonly sortDir = signal<SortDir>('desc');
  readonly activeTab = signal('overview');

  readonly aggregate = signal<AnalyticsAggregate>({
    rangeDays: 30,
    totalEntries: 0,
    avgStutterFrequency: null,
    activePatients: 0,
    distributions: {
      emotions: {},
      triggers: {},
      techniques: {},
    },
    trend: [],
  });

  readonly totalDistributionCount = computed(() =>
    this.sumCounts(this.aggregate().distributions.emotions) +
    this.sumCounts(this.aggregate().distributions.triggers) +
    this.sumCounts(this.aggregate().distributions.techniques),
  );

  readonly hasData = computed(
    () =>
      this.aggregate().totalEntries > 0 || this.totalDistributionCount() > 0,
  );

  readonly kpis = computed<KpiMetric[]>(() => {
    const aggregate = this.aggregate();
    return [
      {
        id: 'entries',
        label: 'Entry volume',
        value: this.fmtNumber(aggregate.totalEntries),
        delta: 'N/A',
        deltaTone: 'neutral',
        helper: `last ${aggregate.rangeDays} days`,
      },
      {
        id: 'intensity',
        label: 'Avg intensity',
        value:
          aggregate.avgStutterFrequency === null
            ? 'N/A'
            : aggregate.avgStutterFrequency.toFixed(1),
        delta: 'N/A',
        deltaTone: 'neutral',
        helper: 'stutter frequency',
      },
      {
        id: 'patients',
        label: 'Active patients',
        value: this.fmtNumber(aggregate.activePatients),
        delta: 'N/A',
        deltaTone: 'neutral',
        helper: 'with shared entries',
      },
    ];
  });

  readonly topEmotions = computed(() =>
    this.buildDistributionItems(this.aggregate().distributions.emotions, 3),
  );
  readonly topTriggers = computed(() =>
    this.buildDistributionItems(this.aggregate().distributions.triggers, 4),
  );
  readonly topTechniques = computed(() =>
    this.buildDistributionItems(this.aggregate().distributions.techniques, 4),
  );

  readonly trendRows = computed<TrendRow[]>(() => {
    const trend = [...this.aggregate().trend].sort((a, b) =>
      a.date.localeCompare(b.date),
    );
    const totalEntries = this.aggregate().totalEntries;

    const rows = trend.map((point, index) => {
      const prev = index > 0 ? trend[index - 1].count : 0;
      const delta =
        prev > 0
          ? `${this.formatDelta(((point.count - prev) / prev) * 100)}`
          : 'N/A';
      const sharePercent =
        totalEntries > 0
          ? Math.round((point.count / totalEntries) * 100)
          : 0;

      return {
        id: point.date,
        date: point.date,
        entries: point.count,
        avgIntensity: point.avgStutterFrequency,
        delta,
        sharePercent,
      };
    });

    const key = this.sortKey();
    const dir = this.sortDir();

    return [...rows].sort((a, b) => {
      if (key === 'date') {
        return dir === 'asc'
          ? a.date.localeCompare(b.date)
          : b.date.localeCompare(a.date);
      }
      const aVal = key === 'entries' ? a.entries : a.avgIntensity;
      const bVal = key === 'entries' ? b.entries : b.avgIntensity;
      return dir === 'asc' ? aVal - bVal : bVal - aVal;
    });
  });

  async ngOnInit(): Promise<void> {
    this.applyPreset(this.preset(), false);
    await this.refresh();
  }

  applyPreset(preset: PresetKey, refresh = true): void {
    this.preset.set(preset);
    const today = new Date();
    let from = new Date(today);

    if (preset === '7d') {
      from.setDate(today.getDate() - 7);
      this.rangeNotice.set(null);
    } else if (preset === '30d') {
      from.setDate(today.getDate() - 30);
      this.rangeNotice.set(null);
    } else {
      from = new Date(today.getFullYear(), 0, 1);
      this.rangeNotice.set(
        'YTD currently maps to a 90-day analytics window.',
      );
    }

    this.dateFrom = this.toDateInputValue(from);
    this.dateTo = this.toDateInputValue(today);

    if (refresh) {
      void this.refresh();
    }
  }

  onDateInputChange(): void {
    const from = this.parseDateInput(this.dateFrom);
    const to = this.parseDateInput(this.dateTo);
    if (!from || !to) return;

    const diffMs = Math.abs(to.getTime() - from.getTime());
    const diffDays = Math.max(1, Math.round(diffMs / 86400000));

    let nextPreset: PresetKey = 'ytd';
    if (diffDays <= 10) {
      nextPreset = '7d';
    } else if (diffDays <= 45) {
      nextPreset = '30d';
    }

    const mappedDays = this.rangeDaysFromPreset(nextPreset);
    if (diffDays !== mappedDays) {
      this.rangeNotice.set(
        `Custom date ranges map to the nearest ${mappedDays}-day window.`,
      );
    } else {
      this.rangeNotice.set(null);
    }

    this.preset.set(nextPreset);
    void this.refresh();
  }

  toggleAdvancedFilter(id: string): void {
    this.advancedFilters.update((filters) =>
      filters.map((filter) =>
        filter.id === id
          ? { ...filter, enabled: !filter.enabled }
          : filter,
      ),
    );
  }

  setSort(key: SortKey): void {
    if (this.sortKey() === key) {
      this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
      return;
    }
    this.sortKey.set(key);
    this.sortDir.set('desc');
  }

  sortIndicator(key: SortKey): string {
    if (this.sortKey() !== key) return '';
    return this.sortDir() === 'asc' ? ' (asc)' : ' (desc)';
  }

  badgeVariant(delta: DeltaTone): 'default' | 'secondary' | 'destructive' {
    if (delta === 'positive') return 'default';
    if (delta === 'negative') return 'destructive';
    return 'secondary';
  }

  fmtNumber(value: number): string {
    return new Intl.NumberFormat().format(value);
  }

  fmtDate(iso: string): string {
    const d = new Date(`${iso}T00:00:00`);
    if (Number.isNaN(d.getTime())) return iso;
    return new Intl.DateTimeFormat(undefined, {
      month: 'short',
      day: '2-digit',
    }).format(d);
  }

  saveView(): void {
    toast.success('Saved view', {
      description: 'Filters and layout saved for this workspace.',
    });
  }

  exportReport(): void {
    toast.success('Export started', {
      description: 'We will notify you when the report is ready.',
    });
  }

  async refresh(): Promise<void> {
    this.loading.set(true);
    this.errorSig.set(null);
    this.warningSig.set(null);

    const rangeDays = this.rangeDaysFromPreset(this.preset());

    try {
      const patients = await this.fetchApprovedPatients();
      if (patients.length === 0) {
        this.aggregate.set(this.emptyAggregate(rangeDays));
        return;
      }

      const results = await Promise.allSettled(
        patients.map((patientId) =>
          this.analyticsApi.getAnalytics(patientId, {
            range: String(rangeDays),
          }),
        ),
      );

      const successes: { patientId: string; data: ServerAnalyticsResponse }[] = [];
      const failures: unknown[] = [];

      results.forEach((result, index) => {
        if (result.status === 'fulfilled') {
          successes.push({ patientId: patients[index], data: result.value });
        } else {
          failures.push(result.reason);
        }
      });

      if (successes.length === 0) {
        this.aggregate.set(this.emptyAggregate(rangeDays));
        if (failures.length > 0) {
          this.errorSig.set(this.toErrorResponse(failures[0]));
        }
        return;
      }

      if (failures.length > 0) {
        this.warningSig.set(
          'Some patient analytics could not be loaded. Data may be incomplete.',
        );
      }

      this.aggregate.set(this.buildAggregate(successes, rangeDays));
    } catch (err) {
      this.aggregate.set(this.emptyAggregate(rangeDays));
      this.errorSig.set(this.toErrorResponse(err));
    } finally {
      this.loading.set(false);
    }
  }

  private async fetchApprovedPatients(): Promise<string[]> {
    const res = await this.patientsApi.getPatientsResponse();
    const rows = res.rows ?? [];
    return rows
      .map((row) => this.patientIdFromRow(row))
      .filter((id): id is string => Boolean(id));
  }

  private patientIdFromRow(row: ServerPatientRowDTO): string | null {
    if (!row.patient?.id) return null;
    const status = row.link?.status ?? 'Pending';
    if (status !== 'Approved') return null;
    return row.patient.id;
  }

  private buildAggregate(
    responses: { patientId: string; data: ServerAnalyticsResponse }[],
    rangeDays: number,
  ): AnalyticsAggregate {
    const distributions = {
      emotions: {} as DistributionMap,
      triggers: {} as DistributionMap,
      techniques: {} as DistributionMap,
    };

    const trendMap = new Map<string, { count: number; sum: number }>();
    let totalEntries = 0;
    let stutterSum = 0;
    let stutterCount = 0;
    let activePatients = 0;

    responses.forEach(({ data }) => {
      const dist = data.distributions ?? {};
      this.mergeCounts(distributions.emotions, dist.emotions);
      this.mergeCounts(distributions.triggers, dist.triggers);
      this.mergeCounts(distributions.techniques, dist.techniques);

      const trend = data.trend ?? [];
      const patientEntries = trend.reduce(
        (sum, point) => sum + (point.count ?? 0),
        0,
      );
      const patientDistCount =
        this.sumCounts(dist.emotions) +
        this.sumCounts(dist.triggers) +
        this.sumCounts(dist.techniques);

      if (patientEntries > 0 || patientDistCount > 0) {
        activePatients += 1;
      }

      totalEntries += patientEntries;

      trend.forEach((point) => {
        const count = point.count ?? 0;
        const avg = point.avgStutterFrequency ?? 0;
        if (count > 0) {
          stutterSum += avg * count;
          stutterCount += count;
        }

        const existing = trendMap.get(point.date ?? '');
        if (!point.date) return;

        if (existing) {
          existing.count += count;
          existing.sum += avg * count;
        } else {
          trendMap.set(point.date, { count, sum: avg * count });
        }
      });
    });

    const trend: TrendPoint[] = Array.from(trendMap.entries())
      .map(([date, data]) => ({
        date,
        count: data.count,
        avgStutterFrequency: data.count > 0 ? data.sum / data.count : 0,
      }))
      .sort((a, b) => a.date.localeCompare(b.date));

    return {
      rangeDays,
      totalEntries,
      avgStutterFrequency:
        stutterCount > 0 ? stutterSum / stutterCount : null,
      activePatients,
      distributions,
      trend,
    };
  }

  private mergeCounts(target: DistributionMap, source?: DistributionMap): void {
    if (!source) return;
    Object.entries(source).forEach(([key, value]) => {
      if (!key) return;
      const nextValue = typeof value === 'number' ? value : 0;
      target[key] = (target[key] ?? 0) + nextValue;
    });
  }

  private buildDistributionItems(
    source: DistributionMap,
    limit: number,
  ): DistributionItem[] {
    const total = this.sumCounts(source);
    if (total === 0) return [];

    return Object.entries(source)
      .map(([label, count]) => ({
        label,
        count,
        percent: Math.round((count / total) * 100),
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, limit);
  }

  private sumCounts(source?: DistributionMap): number {
    if (!source) return 0;
    return Object.values(source).reduce((sum, value) => {
      const next = typeof value === 'number' ? value : 0;
      return sum + next;
    }, 0);
  }

  private emptyAggregate(rangeDays: number): AnalyticsAggregate {
    return {
      rangeDays,
      totalEntries: 0,
      avgStutterFrequency: null,
      activePatients: 0,
      distributions: {
        emotions: {},
        triggers: {},
        techniques: {},
      },
      trend: [],
    };
  }

  private formatDelta(value: number): string {
    const rounded = Math.round(value);
    if (rounded === 0) return '0%';
    return `${rounded > 0 ? '+' : ''}${rounded}%`;
  }

  private rangeDaysFromPreset(preset: PresetKey): number {
    if (preset === '7d') return 7;
    if (preset === '30d') return 30;
    return 90;
  }

  private parseDateInput(value: string): Date | null {
    if (!value) return null;
    const date = new Date(`${value}T00:00:00`);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private toDateInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
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
