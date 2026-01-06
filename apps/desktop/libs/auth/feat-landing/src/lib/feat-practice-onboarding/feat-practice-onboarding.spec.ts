import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeatPracticeOnboarding } from './feat-practice-onboarding';

describe('FeatPracticeOnboarding', () => {
  let component: FeatPracticeOnboarding;
  let fixture: ComponentFixture<FeatPracticeOnboarding>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatPracticeOnboarding],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatPracticeOnboarding);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
