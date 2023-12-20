import { expect, Locator, Page } from "@playwright/test";

export class OsoitepalveluPage {
  readonly page: Page;
  readonly kieliFilter: Filter;
  readonly oppilaitostyyppiFilter: Filter;
  readonly sijaintiFilter: Filter;
  readonly jarjestamislupaFilter: Filter;
  readonly haeButton: Locator;
  readonly tyhjennaButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.kieliFilter = new Filter(page, "Organisaation kieli");
    this.oppilaitostyyppiFilter = new Filter(page, "Oppilaitostyyppi");
    this.sijaintiFilter = new Filter(page, "Sijainti");
    this.jarjestamislupaFilter = new Filter(
      page,
      "Ammatillisen koulutuksen järjestämislupa"
    );
    this.haeButton = page.getByRole("button", { name: "Hae" });
    this.tyhjennaButton = page.getByRole("button", { name: "Tyhjennä" });
  }

  async goto() {
    await this.page.goto("http://localhost:3003/osoitteet");
  }
}

class Filter {
  readonly page: Page;
  readonly scope: Locator;
  readonly button: Locator;
  readonly contents: Locator;
  readonly selectionIndicator: Locator;

  constructor(page: Page, title: string) {
    this.page = page;
    this.scope = this.page.getByRole("group").filter({
      has: this.page.getByRole("heading", { name: title }),
    });
    this.button = this.scope.getByRole("button").first();
    this.selectionIndicator = this.button.locator("[aria-live=off]");
    this.contents = this.scope.getByRole("group");
  }

  async clear() {
    const isOpen = await this.isOpen();
    if (!isOpen) {
      await this.open();
    }
    await expect(this.contents).toBeVisible();

    for (const checkbox of await this.contents
      .locator("label", {
        has: this.page.getByRole("checkbox", {
          checked: true,
          includeHidden: true,
        }),
      })
      .all()) {
      await checkbox.click();
    }
    if (!isOpen) {
      await this.close();
    }
  }

  async isOpen() {
    return await this.contents.isVisible();
  }

  async open() {
    await this.pressTabUntilFocusOn(this.button);
    await this.page.keyboard.press("Space");
  }

  async close() {
    await this.pressTabUntilFocusOn(this.button);
    await this.page.keyboard.press("Space");
  }

  async pressTabUntilFocusOn(locator: Locator) {
    let correctLocatorHasFocus = false;
    while (!correctLocatorHasFocus) {
      correctLocatorHasFocus = await locator.evaluate(
        (node) => document.activeElement == node
      );
      if (!correctLocatorHasFocus) {
        await this.page.keyboard.press("Tab");
      }
    }
  }

  async toggleCheckboxByLabel(label: string) {
    const checkbox = await this.contents.getByLabel(label, { exact: true });
    await this.pressTabUntilFocusOn(checkbox);
    await this.page.keyboard.press("Space");
  }
}
