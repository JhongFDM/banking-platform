import { createBdd } from 'playwright-bdd';
import { expect } from '@playwright/test';
const { Given, When, Then } = createBdd();

const validCredentials = {
  username: 'valid.user@example.com',
  password: 'ValidPass123!'
};

Given('the user is on the login page', async ({ page }) => {
  await page.route('**/api/auth/login', async (route) => {
    const body = route.request().postDataJSON() as { username?: string; password?: string } | null;
    const isValidLogin =
      body?.username === validCredentials.username &&
      body?.password === validCredentials.password;

    if (isValidLogin) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          accessToken: 'fake-access-token',
          tokenType: 'Bearer',
          roles: ['CUSTOMER'],
          customerId: '123'
        })
      });
      return;
    }

    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({
        message: 'Invalid credentials'
      })
    });
  });

  await page.goto('http://localhost:5173/login');
  expect(page.url()).toContain('/login');
});

Given('I open page {string}', async ({ page }, url: string) => {
  await page.goto(url);
});

When('the user enters valid credentials', async ({ page }) => {
  await page.locator('#login-username').fill(validCredentials.username);
  await page.locator('#login-password').fill(validCredentials.password);
});

When('the user enters invalid credentials', async ({ page }) => {
  await page.locator('#login-username').fill('invalid.user@example.com');
  await page.locator('#login-password').fill('WrongPass123!');
});

When('clicks the login button', async ({ page }) => {
  await page.getByRole('button', { name: 'Sign In' }).click();
});

Then('the user should be redirected to the dashboard', async ({ page }) => {
  await expect(page).toHaveURL(/\/customer\/123\/accounts/);
});

Then('an error message should be displayed', async ({ page }) => {
  await expect(page.locator('.banner.error')).toBeVisible();
});