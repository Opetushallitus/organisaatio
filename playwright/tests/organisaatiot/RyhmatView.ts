import { Locator, Page } from "@playwright/test";
import { FormView } from "../FormView";

export class RyhmatView {
  readonly page: Page;
  readonly uusiRyhmaButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.uusiRyhmaButton = page.getByText("+ RYHMAT_LISAA_UUSI_FI");
  }

  async goto() {
    await this.page.goto(`http://localhost:3003/organisaatio-service/ryhmat`);
  }

  ryhmaLink(name: string) {
    return this.page.locator("a", { hasText: name });
  }
}

export class RyhmaEditView extends FormView {
  readonly page: Page;
  readonly tallennaButton: Locator;

  constructor(page: Page) {
    super(page);
    this.page = page;
    this.tallennaButton = page.getByText("BUTTON_TALLENNA");
  }

  async gotoUusiRyhma() {
    await this.page.goto(
      `http://localhost:3003/organisaatio-service/ryhmat/uusi`
    );
  }

  async selectRyhmanTyyppi(value: string) {
    await this.page
      .locator("#RYHMALOMAKE_RYHMAN_TYYPPI_SELECT input[type=text]")
      .focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(value);
    await this.page.keyboard.press("Enter");
  }

  async selectRyhmanKayttotarkoitus(value: string) {
    await this.page
      .locator("#RYHMALOMAKE_RYHMAN_KAYTTOTARKOITUS_SELECT input[type=text]")
      .focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(value);
    await this.page.keyboard.press("Enter");
  }
}
