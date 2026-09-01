import { expect, type Locator, type Page } from '@playwright/test';
import { BasePage } from '../BasePage';

class LoginPage extends BasePage {
    emailField: Locator;
    passwordField: Locator;
    signinButton: Locator;

    constructor(page: Page) {
        super(page, '/login');
        this.emailField = page.locator('#email');
        this.passwordField = page.locator('#password');
        this.signinButton = page.locator('#login-button');
    }

}
