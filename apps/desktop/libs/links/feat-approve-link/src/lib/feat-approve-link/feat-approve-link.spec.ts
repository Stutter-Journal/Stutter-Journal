import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatApproveLink } from './feat-approve-link';

describe('FeatApproveLink', () => {
  let component: FeatApproveLink;
  let fixture: ComponentFixture<FeatApproveLink>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatApproveLink],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatApproveLink);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
