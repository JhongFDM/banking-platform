import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StandingOrders } from './standing-orders';

describe('StandingOrders', () => {
  let component: StandingOrders;
  let fixture: ComponentFixture<StandingOrders>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StandingOrders],
    }).compileComponents();

    fixture = TestBed.createComponent(StandingOrders);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
