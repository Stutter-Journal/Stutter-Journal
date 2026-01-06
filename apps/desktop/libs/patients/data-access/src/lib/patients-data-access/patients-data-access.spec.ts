import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatientsDataAccess } from './patients-data-access';

describe('PatientsDataAccess', () => {
  let component: PatientsDataAccess;
  let fixture: ComponentFixture<PatientsDataAccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientsDataAccess],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientsDataAccess);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
