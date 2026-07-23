import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpendingInsights } from './spending-insights';

describe('SpendingInsights', () => {
  let component: SpendingInsights;
  let fixture: ComponentFixture<SpendingInsights>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpendingInsights],
    }).compileComponents();

    fixture = TestBed.createComponent(SpendingInsights);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
