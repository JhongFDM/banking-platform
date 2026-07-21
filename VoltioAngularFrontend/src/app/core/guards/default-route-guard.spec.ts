import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { defaultRouteGuard } from './default-route-guard';

describe('defaultRouteGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => defaultRouteGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
