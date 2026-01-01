import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatInvitePatient } from './feat-invite-patient';

describe('FeatInvitePatient', () => {
  let component: FeatInvitePatient;
  let fixture: ComponentFixture<FeatInvitePatient>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatInvitePatient],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatInvitePatient);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
