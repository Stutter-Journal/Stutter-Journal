import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LinksDataAccess } from './links-data-access';

describe('LinksDataAccess', () => {
  let component: LinksDataAccess;
  let fixture: ComponentFixture<LinksDataAccess>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LinksDataAccess],
    }).compileComponents();

    fixture = TestBed.createComponent(LinksDataAccess);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
