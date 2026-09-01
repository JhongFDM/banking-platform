
class BasePage {
    readonly Page: any;
    readonly URL: string = "http://localhost:5173";

    async goto() {
        await this.Page.goto(this.URL);
    }

    constructor(Page: any, url: string) {
        this.Page = Page;
        this.URL = this.URL + url;
    }

}

export default BasePage;