import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatCascade } from './feat-cascade';

describe('FeatCascade', () => {
  let component: FeatCascade;
  let fixture: ComponentFixture<FeatCascade>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatCascade],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatCascade);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
