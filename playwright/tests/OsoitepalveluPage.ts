import { expect, Locator, Page } from "@playwright/test";

export class OsoitepalveluPage {
  readonly page: Page;
  readonly kieliFilter: KieliFilter;

  constructor(page: Page) {
    this.page = page;
    this.kieliFilter = new KieliFilter(page);
  }

  async goto() {
    await this.page.goto("http://localhost:3003/osoitteet");
  }
}

class KieliFilter {
  readonly page: Page;
  readonly scope: Locator;
  readonly button: Locator;
  readonly selectionIndicator: Locator;

  constructor(page: Page) {
    this.page = page;
    this.scope = this.page.getByRole("group").filter({
      has: this.page.getByRole("heading", { name: "Organisaation kieli" }),
    });
    this.button = this.scope.getByRole("button");
    this.selectionIndicator = this.button.locator("[aria-live=off]");
  }

  async clear() {
    const isOpen = await this.isOpen();
    if (!isOpen) {
      await this.open();
    }
    const contents = this.page.getByRole("heading", {
      name: "Valitse organisaatiot, joiden kieli on:",
    });
    await expect(contents).toBeVisible();

    for (const checkbox of await this.scope
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
    const isVisible = await this.page
      .getByRole("heading", {
        name: "Valitse organisaatiot, joiden kieli on:",
      })
      .isVisible();
    return isVisible;
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
}
