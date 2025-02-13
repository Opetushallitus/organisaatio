import { Locator, Page } from "@playwright/test";
import { TableView } from "../TableView";
import { selectAll } from "../LexicalUtil";

export class OrganisaatiotView extends TableView {
  readonly page: Page;
  readonly searchField: Locator;

  constructor(page: Page) {
    super(page);
    this.page = page;
    this.searchField = page.getByPlaceholder(
      "TAULUKKO_TOIMIJA_HAKU_PLACEHOLDER_FI"
    );
  }

  async goto() {
    await this.page.goto(
      `http://localhost:3003/organisaatio-service/organisaatiot`
    );
  }

  organisaatioLink(name: string) {
    return this.page.locator("a", { hasText: name });
  }

  async filterByName(name: string) {
    await this.searchField.focus();
    await selectAll(this.page);
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(name);
    await this.page.keyboard.press("Enter");
  }
}
