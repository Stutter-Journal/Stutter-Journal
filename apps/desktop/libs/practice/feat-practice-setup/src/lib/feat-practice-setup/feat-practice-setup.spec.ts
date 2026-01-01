import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatPracticeSetup } from './feat-practice-setup';

describe('FeatPracticeSetup', () => {
  let component: FeatPracticeSetup;
  let fixture: ComponentFixture<FeatPracticeSetup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatPracticeSetup],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatPracticeSetup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
