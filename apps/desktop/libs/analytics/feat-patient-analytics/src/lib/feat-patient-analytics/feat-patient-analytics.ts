import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal } from '@angular/core';
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

type DeltaTone = 'positive' | 'negative' | 'neutral';
type PresetKey = '7d' | '30d' | 'ytd';
type TableView = 'pages' | 'users';
type SortKey = 'name' | 'sessions' | 'intensity' | 'errors';
type SortDir = 'asc' | 'desc';

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

interface TableRow {
  id: string;
  name: string;
  subtitle: string;
  sessions: number;
  intensity: number;
  errors: number;
  delta: string;
  summary: string;
  tags: string[];
}

interface CohortRow {
  id: string;
  label: string;
  count: number;
  intensity: number;
  change: string;
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
  ],
  templateUrl: './feat-patient-analytics.html',
})
export class FeatPatientAnalytics implements OnInit {
  readonly loading = signal(true);

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

  readonly kpis: KpiMetric[] = [
    {
      id: 'entries',
      label: 'Entry volume',
      value: '1,248',
      delta: '+12%',
      deltaTone: 'positive',
      helper: 'vs previous period',
    },
    {
      id: 'intensity',
      label: 'Avg intensity',
      value: '5.4',
      delta: '-3%',
      deltaTone: 'negative',
      helper: 'emotional load',
    },
    {
      id: 'patients',
      label: 'Active patients',
      value: '86',
      delta: '+8%',
      deltaTone: 'positive',
      helper: 'shared entries',
    },
  ];

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

  readonly tableView = signal<TableView>('pages');
  readonly sortKey = signal<SortKey>('sessions');
  readonly sortDir = signal<SortDir>('desc');
  readonly activeTab = signal('overview');

  private readonly topPages: TableRow[] = [
    {
      id: 'page-1',
      name: 'Morning grounding',
      subtitle: 'Daily check-in',
      sessions: 320,
      intensity: 4.2,
      errors: 9,
      delta: '+6%',
      summary: 'Lower intensity with steady pacing.',
      tags: ['breath', 'routine'],
    },
    {
      id: 'page-2',
      name: 'Presentation prep',
      subtitle: 'Stress rehearsal',
      sessions: 214,
      intensity: 6.7,
      errors: 18,
      delta: '-2%',
      summary: 'High load but more control notes.',
      tags: ['exposure', 'speech plan'],
    },
    {
      id: 'page-3',
      name: 'Social check-in',
      subtitle: 'Peer conversations',
      sessions: 188,
      intensity: 5.1,
      errors: 7,
      delta: '+10%',
      summary: 'Gradual gains in confidence.',
      tags: ['community', 'support'],
    },
    {
      id: 'page-4',
      name: 'Breath reset',
      subtitle: 'Technique log',
      sessions: 156,
      intensity: 3.6,
      errors: 5,
      delta: '+4%',
      summary: 'Short entries, consistent progress.',
      tags: ['breathing', 'reset'],
    },
  ];

  private readonly topUsers: TableRow[] = [
    {
      id: 'user-1',
      name: 'Patient A',
      subtitle: 'Plan: Calm track',
      sessions: 92,
      intensity: 6.1,
      errors: 3,
      delta: '+14%',
      summary: 'More entries during high-stress days.',
      tags: ['weekly', 'pattern'],
    },
    {
      id: 'user-2',
      name: 'Patient B',
      subtitle: 'Plan: Fluency lab',
      sessions: 85,
      intensity: 4.7,
      errors: 2,
      delta: '+5%',
      summary: 'Stable improvements with breathing.',
      tags: ['breath', 'steady'],
    },
    {
      id: 'user-3',
      name: 'Patient C',
      subtitle: 'Plan: Confidence',
      sessions: 74,
      intensity: 7.2,
      errors: 6,
      delta: '-1%',
      summary: 'Needs follow-up on triggers.',
      tags: ['trigger', 'follow-up'],
    },
  ];

  readonly cohorts: CohortRow[] = [
    { id: 'cohort-1', label: 'Newly enrolled', count: 24, intensity: 6.3, change: '+9%' },
    { id: 'cohort-2', label: '6+ months active', count: 38, intensity: 4.8, change: '-4%' },
    { id: 'cohort-3', label: 'Maintenance phase', count: 19, intensity: 3.9, change: '+2%' },
  ];

  readonly tableRows = computed(() => {
    const rows =
      this.tableView() === 'pages' ? this.topPages : this.topUsers;
    const key = this.sortKey();
    const dir = this.sortDir();

    return [...rows].sort((a, b) => {
      const aVal = a[key];
      const bVal = b[key];
      if (typeof aVal === 'string' && typeof bVal === 'string') {
        return dir === 'asc'
          ? aVal.localeCompare(bVal)
          : bVal.localeCompare(aVal);
      }
      if (typeof aVal === 'number' && typeof bVal === 'number') {
        return dir === 'asc' ? aVal - bVal : bVal - aVal;
      }
      return 0;
    });
  });

  async ngOnInit(): Promise<void> {
    await Promise.resolve();
    this.loading.set(false);
    this.applyPreset(this.preset());
  }

  applyPreset(preset: PresetKey): void {
    this.preset.set(preset);
    const today = new Date();
    let from = new Date(today);

    if (preset === '7d') {
      from.setDate(today.getDate() - 7);
    } else if (preset === '30d') {
      from.setDate(today.getDate() - 30);
    } else {
      from = new Date(today.getFullYear(), 0, 1);
    }

    this.dateFrom = this.toDateInputValue(from);
    this.dateTo = this.toDateInputValue(today);
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

  setTableView(view: TableView): void {
    this.tableView.set(view);
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

  deltaTone(value: string): DeltaTone {
    if (value.startsWith('+')) return 'positive';
    if (value.startsWith('-')) return 'negative';
    return 'neutral';
  }

  fmtNumber(value: number): string {
    return new Intl.NumberFormat().format(value);
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

  private toDateInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
