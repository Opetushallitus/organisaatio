import { Locator, Page } from "@playwright/test";
import { FormView } from "../FormView";

export class RyhmatView {
  readonly page: Page;
  readonly uusiRyhmaButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.uusiRyhmaButton = page.getByTestId("new-ryhma-button");
  }

  async goto() {
    await this.page.goto(`http://localhost:3003/organisaatio-service/ryhmat`);
  }

  ryhmaLink(name: string) {
    return this.page.locator("a", { hasText: name });
  }

  async filterByRyhmatyyppi(value: string) {
    await this.page.locator("#RYHMAN_TYYPPI_SELECT input[type=text]").focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(value);
    await this.page.keyboard.press("Enter");
  }

  async clearRyhmatyyppi() {
    await this.page.locator("#RYHMAN_TYYPPI_SELECT input[type=text]").focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Enter");
  }

  async filterByKayttotarkoitus(value: string) {
    await this.page
      .locator("#RYHMAN_KAYTTOTARKOITUS_SELECT input[type=text]")
      .focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(value);
    await this.page.keyboard.press("Enter");
  }

  async clearKayttotarkoitus() {
    await this.page
      .locator("#RYHMAN_KAYTTOTARKOITUS_SELECT input[type=text]")
      .focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Enter");
  }

  async filterByTila(value: string) {
    await this.page.locator("#RYHMAN_TILA_SELECT input[type=text]").focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(value);
    await this.page.keyboard.press("Enter");
  }

  async clearTila() {
    await this.page.locator("#RYHMAN_TILA_SELECT input[type=text]").focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Enter");
  }

  async setShownOnPage(amount: string) {
    await this.page
      .locator(`option[value="10"]`)
      .locator("..")
      .selectOption(amount);
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
