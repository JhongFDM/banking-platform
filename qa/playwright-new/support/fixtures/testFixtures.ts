import { test as base, createBdd } from 'playwright-bdd';

import {LoginPage} from '../../src/pages/auth/LoginPage';
import {RegistrationPage} from '../../src/pages/auth/RegistrationPage';

type AppPages = {
    loginPage: LoginPage;
    registrationPage: RegistrationPage;
    screenshot: (options: {path: string}) => Promise<void>;
};

export const test = base.extend<{pages: AppPages}>({
    pages: async ({ page }, use) => {
        await use({
            loginPage: new LoginPage(page),
            registrationPage: new RegistrationPage(page),

            screenshot: async (options: {path: string}) => {
                await page.screenshot(options);
            }
        });
    }
});

export const { Given, When, Then } = createBdd(test);
