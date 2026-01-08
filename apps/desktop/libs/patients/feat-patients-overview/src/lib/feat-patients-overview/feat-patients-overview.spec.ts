import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatPatientsOverview } from './feat-patients-overview';

describe('FeatPatientsOverview', () => {
  let component: FeatPatientsOverview;
  let fixture: ComponentFixture<FeatPatientsOverview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatPatientsOverview],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatPatientsOverview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
