import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthlyStatements } from './monthly-statements';

describe('MonthlyStatements', () => {
  let component: MonthlyStatements;
  let fixture: ComponentFixture<MonthlyStatements>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MonthlyStatements],
    }).compileComponents();

    fixture = TestBed.createComponent(MonthlyStatements);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
