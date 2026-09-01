import { expect, type Locator, type Page } from '@playwright/test';

export class BasePage {
    readonly page: Page;
    readonly url: string = "http://localhost:5173";

    async goto(){
        await this.page.goto(this.url);
    }

    constructor(page: Page, url: string) {
        this.page = page;
        this.url = this.url + '/' + url;
    }
}