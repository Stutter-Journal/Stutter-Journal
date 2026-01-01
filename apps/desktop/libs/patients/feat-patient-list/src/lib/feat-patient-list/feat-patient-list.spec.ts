import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatPatientList } from './feat-patient-list';

describe('FeatPatientList', () => {
  let component: FeatPatientList;
  let fixture: ComponentFixture<FeatPatientList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatPatientList],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatPatientList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
