import { Page } from "@playwright/test";

export class TableView {
  readonly page: Page;
  amount: number;

  constructor(page: Page) {
    this.page = page;
    this.amount = 10;
  }

  async setPageNumber(page: number) {
    await this.page.locator(`button:text("${page}")`).click();
  }

  async setShownOnPage(newAmount: number) {
    await this.page
      .locator(`option[value="${this.amount}"]`)
      .locator("..")
      .selectOption(`${newAmount}`);
    this.amount = newAmount;
  }
}
