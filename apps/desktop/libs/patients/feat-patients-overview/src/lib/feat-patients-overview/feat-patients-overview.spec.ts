import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatPatientsOverview } from './feat-patients-overview';
import { PatientsClientService } from '@org/patients-data-access';
import { computed, signal } from '@angular/core';

describe('FeatPatientsOverview', () => {
  let component: FeatPatientsOverview;
  let fixture: ComponentFixture<FeatPatientsOverview>;

  beforeEach(async () => {
    const loadingSig = signal(false);
    const errorSig = signal(null);

    await TestBed.configureTestingModule({
      imports: [FeatPatientsOverview],
      providers: [
        {
          provide: PatientsClientService,
          useValue: {
            loading: computed(() => loadingSig()),
            error: computed(() => errorSig()),
            getPatientsResponse: async () => ({ patients: [], pendingLinks: [] }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatPatientsOverview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
