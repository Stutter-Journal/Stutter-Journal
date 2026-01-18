import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatPatientAnalytics } from './feat-patient-analytics';
import { AnalyticsClientService } from '@org/analytics-data-access';
import { PatientsClientService } from '@org/patients-data-access';

describe('FeatPatientAnalytics', () => {
  let component: FeatPatientAnalytics;
  let fixture: ComponentFixture<FeatPatientAnalytics>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatPatientAnalytics],
      providers: [
        {
          provide: AnalyticsClientService,
          useValue: {
            getAnalytics: async () => ({
              distributions: { emotions: {}, triggers: {}, techniques: {} },
              trend: [],
              rangeDays: 7,
            }),
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

    fixture = TestBed.createComponent(FeatPatientAnalytics);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
