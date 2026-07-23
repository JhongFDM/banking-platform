import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllAccounts } from './all-accounts';

describe('AllAccounts', () => {
  let component: AllAccounts;
  let fixture: ComponentFixture<AllAccounts>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AllAccounts],
    }).compileComponents();

    fixture = TestBed.createComponent(AllAccounts);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
