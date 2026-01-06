import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeatEntryList } from './feat-entry-list';

describe('FeatEntryList', () => {
  let component: FeatEntryList;
  let fixture: ComponentFixture<FeatEntryList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeatEntryList],
    }).compileComponents();

    fixture = TestBed.createComponent(FeatEntryList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
