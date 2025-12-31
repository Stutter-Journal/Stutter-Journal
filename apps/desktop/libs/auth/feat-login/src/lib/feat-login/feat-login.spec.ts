import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatLogin } from './feat-login';

describe('FeatLogin', () => {
  let component: FeatLogin;
  let fixture: ComponentFixture<FeatLogin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatLogin],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatLogin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
