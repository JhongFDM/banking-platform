import { expect } from '@playwright/test';
import { Given, When, Then } from '../../../../support/fixtures/testFixtures';

const validCredentials = {
  username: 'Test@example.com',
  password: 'Test@1234'
};

Given('the user is on the login page', async ({ page, pages }) => {
    await pages.loginPage.goto();

    expect(page.url()).toContain('/login');
});

When('the user enters valid credentials', async ({ page, pages }) => {
    await pages.loginPage.enterEmail(validCredentials.username);
    await pages.loginPage.enterPassword(validCredentials.password);
});

When('clicks the login button', async ({ page, pages }) => {
    await pages.loginPage.clickSignIn();
});

Then('the user should be redirected to the dashboard', async ({ page, pages }) => {
    await expect(page).toHaveURL(/.*customer/);
    await page.screenshot({ path: 'dashboard.png' });
});

When('the user enters the username {string} and password {string}', async ({ page, pages }, username, password) => {
    await pages.loginPage.enterEmail(username);
    await pages.loginPage.enterPassword(password);
});

Then('an error message should be displayed indicating invalid credentials', async ({ page, pages }) => {
    //const errorMessage = await pages.loginPage.getErrorMessage();
    //expect(errorMessage).toBe('Invalid username or password.');
    await pages.loginPage.credentialErrorMessage.waitFor({ state: 'visible' });
    await page.screenshot({ path: 'error.png' });
});