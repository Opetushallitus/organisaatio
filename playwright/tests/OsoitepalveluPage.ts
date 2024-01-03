import { expect, Locator, Page } from "@playwright/test";

export class OsoitepalveluPage {
  readonly page: Page;
  readonly kohderyhmaInstructions: Locator;
  readonly filterInstructions: Locator;
  readonly kieliFilter: Filter;
  readonly oppilaitostyyppiFilter: Filter;
  readonly sijaintiFilter: Filter;
  readonly jarjestamislupaFilter: Filter;
  readonly haeButton: Locator;
  readonly tyhjennaButton: Locator;
  readonly koulutustoimijatCheckbox: Locator;
  readonly koulutusotimijatKohderyhma: Kohderyhma;
  readonly oppilaitoksetKohderyhma: Kohderyhma;
  readonly oppilaitostentoimipisteetKohderyhma: Kohderyhma;

  constructor(page: Page) {
    this.page = page;
    this.kohderyhmaInstructions = page.locator("h2").nth(0);
    this.filterInstructions = page.locator("h2").nth(1);
    this.koulutustoimijatCheckbox = page.getByLabel("Koulutustoimijat", {
      exact: true,
    });
    this.koulutusotimijatKohderyhma = new Kohderyhma(page, "Koulutustoimijat");
    this.oppilaitoksetKohderyhma = new Kohderyhma(page, "Oppilaitokset");
    this.oppilaitostentoimipisteetKohderyhma = new Kohderyhma(
      page,
      "Oppilaitosten toimipisteet"
    );
    this.kieliFilter = new Filter(page, "Organisaation kieli");
    this.oppilaitostyyppiFilter = new Filter(page, "Oppilaitostyyppi");
    this.sijaintiFilter = new Filter(page, "Sijainti");
    this.jarjestamislupaFilter = new Filter(
      page,
      "Ammatillisen koulutuksen järjestämislupa"
    );
    this.haeButton = page.getByRole("button", { name: "Hae" });
    this.tyhjennaButton = page.getByRole("button", {
      name: "Tyhjennä",
      exact: true,
    });
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
  readonly checkedCheckboxLabels: Locator;

  constructor(page: Page, title: string) {
    this.page = page;
    this.scope = this.page.getByRole("group").filter({
      has: this.page.getByRole("heading", { name: title }),
    });
    this.button = this.scope.getByRole("button").first();
    this.selectionIndicator = this.button.locator("[aria-live=off]");
    this.contents = this.scope.getByRole("group");
    this.checkedCheckboxLabels = this.contents
      .locator("label")
      .filter({ has: page.getByRole("checkbox", { checked: true }) });
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
    await this.pressSpaceOnLocator(this.button);
  }

  async close() {
    await this.pressSpaceOnLocator(this.button);
  }

  async toggleCheckboxByLabel(label: string) {
    const checkbox = await this.contents.getByLabel(label, { exact: true });
    await this.pressSpaceOnLocator(checkbox);
  }

  async pressSpaceOnLocator(locator: Locator) {
    return pressSpaceOnLocator(this.page, locator);
  }
}

class Kohderyhma {
  readonly page: Page;
  readonly checkbox: Locator;

  constructor(page: Page, label: string) {
    this.page = page;
    this.checkbox = page.getByLabel(label);
  }

  async toggle() {
    await pressSpaceOnLocator(this.page, this.checkbox);
  }
}

async function pressSpaceOnLocator(page: Page, locator: Locator) {
  await pressTabUntilFocusOn(page, locator);
  return page.keyboard.press("Space");
}

async function pressTabUntilFocusOn(page: Page, locator: Locator) {
  let correctLocatorHasFocus = false;
  while (!correctLocatorHasFocus) {
    correctLocatorHasFocus = await locator.evaluate(
      (node) => document.activeElement == node
    );
    if (!correctLocatorHasFocus) {
      await page.keyboard.press("Tab");
    }
  }
}
