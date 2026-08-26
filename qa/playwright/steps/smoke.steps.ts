import { createBdd } from 'playwright-bdd';
import { test, expect } from '../fixtures';

const { Given, Then } = createBdd(test);

Given('I open the application home page', async ({ page }) => {
  await page.goto('data:text/html,<title>Smoke Test</title><h1>ok</h1>');
});

Then('the page title should not be empty', async ({ page }) => {
  await expect(page).toHaveTitle(/.+/);
});
