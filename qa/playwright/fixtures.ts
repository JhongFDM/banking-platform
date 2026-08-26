import { test as base } from 'playwright-bdd';
import { expect } from '@playwright/test';

// Shared test object for BDD step bindings.
export const test = base;
export { expect };
