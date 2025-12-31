import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatRegister } from './feat-register';

describe('FeatRegister', () => {
  let component: FeatRegister;
  let fixture: ComponentFixture<FeatRegister>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatRegister],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatRegister);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
