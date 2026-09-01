import { expect } from '@playwright/test';
import { Given, When, Then } from '../../../../support/fixtures/testFixtures';

// await pages.screenshot({ path: 'playwright-report/screenshots/register-business.png' });

When('I select {string} account and click continue', async ({ page, pages }, arg: string) => {
  if (arg === 'business')
    await pages.registrationPage.accountType.selectOption('Business');

  await pages.registrationPage.continueButton.click();

  if (arg === 'personal')
    expect(await pages.registrationPage.govBusinessId.isVisible()).toBe(false);
  else if (arg === 'business')
    expect(await pages.registrationPage.govBusinessId.isVisible()).toBe(true);
});
