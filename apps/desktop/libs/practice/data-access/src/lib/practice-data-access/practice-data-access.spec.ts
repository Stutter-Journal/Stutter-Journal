import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PracticeDataAccess } from './practice-data-access';

describe('PracticeDataAccess', () => {
  let component: PracticeDataAccess;
  let fixture: ComponentFixture<PracticeDataAccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PracticeDataAccess],
    }).compileComponents();

    fixture = TestBed.createComponent(PracticeDataAccess);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
