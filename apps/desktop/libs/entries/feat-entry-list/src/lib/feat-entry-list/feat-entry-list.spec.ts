import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatEntryList } from './feat-entry-list';
import { EntriesClientService } from '@org/data-access';
import { PatientsClientService } from '@org/patients-data-access';
import { computed, signal } from '@angular/core';

describe('FeatEntryList', () => {
  let component: FeatEntryList;
  let fixture: ComponentFixture<FeatEntryList>;

  beforeEach(async () => {
    const loadingSig = signal(false);
    const errorSig = signal(null);

    await TestBed.configureTestingModule({
      imports: [FeatEntryList],
      providers: [
        {
          provide: EntriesClientService,
          useValue: {
            loading: computed(() => loadingSig()),
            error: computed(() => errorSig()),
            getEntries: async () => [],
          },
        },
        {
          provide: PatientsClientService,
          useValue: {
            getPatientsResponse: async () => ({ rows: [] }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatEntryList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
