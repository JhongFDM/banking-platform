import BasePage from '../BasePage';
import { expect, Locator, type Page } from '@playwright/test';

export class RegistrationPage extends BasePage {
    readonly page: Page;
    readonly accountType: Locator;
    readonly continueButton: Locator;
    readonly email: Locator;
    readonly password: Locator;
    readonly name: Locator;
    readonly address: Locator;
    readonly dob: Locator;
    readonly createAccountButton: Locator;
    readonly govBusinessId: Locator;

    async selectType(type : String){
        if (type == 'business'){
            await this.accountType.selectOption('Business');
        }
        await this.continueButton.click();
    }

    constructor(page: Page){
        super(page, '/register');
        this.accountType = page.getByRole('combobox');
        this.continueButton = page.getByRole('button').getByText('Continue');
        this.email = page.locator('');
        this.password = page.locator('');
        this.name = page.locator('');
        this.address = page.locator('');
        this.dob = page.locator('');
        this.createAccountButton = page.locator('');
        this.govBusinessId = page.getByLabel('Government Business Number');

        this.page = page;

    }

}