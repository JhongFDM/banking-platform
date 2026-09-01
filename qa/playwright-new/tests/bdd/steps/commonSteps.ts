import { expect } from '@playwright/test';
import { Given, When, Then } from '../../../support/fixtures/testFixtures';

Given('the user is on the {string} page', async ({page, pages}, arg: string) => {

    if (arg == 'login')
        await pages.loginPage.goto();
    else if (arg == 'registration')
        await pages.registrationPage.goto();

    await(expect)

  // Step: Given the user is on the 'login' page
  // From: tests\bdd\features\auth\login.feature:4:5
});
