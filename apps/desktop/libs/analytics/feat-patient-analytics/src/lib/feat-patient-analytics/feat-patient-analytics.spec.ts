import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatPatientAnalytics } from './feat-patient-analytics';

describe('FeatPatientAnalytics', () => {
  let component: FeatPatientAnalytics;
  let fixture: ComponentFixture<FeatPatientAnalytics>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatPatientAnalytics],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatPatientAnalytics);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
