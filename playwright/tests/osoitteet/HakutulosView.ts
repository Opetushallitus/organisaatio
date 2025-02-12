import { expect, Locator, Page } from "@playwright/test";

export class HakutulosView {
  readonly page: Page;
  readonly selectAllCheckbox: Locator;
  readonly lataaExcelButton: Locator;
  readonly kirjoitaSahkopostiButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.selectAllCheckbox = page.getByTestId("select-all");
    this.lataaExcelButton = page.getByRole("button", { name: "Lataa Excel" });
    this.kirjoitaSahkopostiButton = page.getByRole("button", {
      name: "Kirjoita sähköpostiviesti",
    });
  }

  async select(oid: string) {
    await this.page.getByTestId(`select-${oid}`).click();
  }
}
