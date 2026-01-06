import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatLanding } from './feat-landing';

describe('FeatLanding', () => {
  let component: FeatLanding;
  let fixture: ComponentFixture<FeatLanding>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatLanding],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatLanding);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
