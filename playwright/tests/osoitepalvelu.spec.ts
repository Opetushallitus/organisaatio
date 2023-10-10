import { expect, Locator, Page, test } from "@playwright/test";

test.describe("Osoitepalvelu", () => {
  test.beforeEach(async ({ page }, testInfo) => {
    await page.goto("http://localhost:3003/osoitteet");
  });

  test("has title", async ({ page }) => {
    await expect(page).toHaveTitle(/Organisaatio/);
  });
  test("allows searching for koulutustoimijat", async ({ page }) => {
    await expect(page.getByText("Hae")).toBeVisible();
    await page.getByText("Hae").click();

    await expect(page.getByText("3 hakutulosta valittu")).toBeVisible();
    await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    await expect(
      page.getByText("Testi Koulutuskuntayhtymä Puolukka")
    ).toBeVisible();
    await expect(page.getByText("Mustikkalan testi yhdistys")).toBeVisible();
  });
  test.describe("Search by oppilaitostyyppi box", () => {
    test("opens and closes", async ({ page }) => {
      const button = page.getByRole("button", { name: "Oppilaitostyyppi" });
      await pressTabUntilFocusOn(page, button);
      await expect(page.getByRole("list")).toBeHidden();
      await page.keyboard.press("Space");
      await expect(page.getByRole("list")).toBeVisible();
      await page.keyboard.press("Space");
      await expect(page.getByRole("list")).toBeHidden();
    });
    test("updates selection text", async ({ page }) => {
      const button = page.getByRole("button", { name: "Oppilaitostyyppi" });
      await pressTabUntilFocusOn(page, button);
      await page.keyboard.press("Space");
      await expect(page.getByRole("list")).toBeVisible();
      await expect(button.locator("[aria-live=off]")).toHaveText("");
      await checkRandomNumberofListItems(page);
      const expectedText = await page
        .getByRole("listitem")
        .filter({ has: page.getByRole("checkbox", { checked: true }) })
        .allTextContents()
        .then((texts) => texts.join(", "));

      await expect(button.locator("[aria-live=off]")).toHaveText(expectedText);
    });
    test("checks and unchecks all selections", async ({ page }) => {
      const button = page.getByRole("button", { name: "Oppilaitostyyppi" });
      await pressTabUntilFocusOn(page, button);
      await page.keyboard.press("Space");
      await expect(
        page
          .getByRole("listitem")
          .filter({ has: page.getByRole("checkbox", { checked: true }) })
      ).toHaveCount(0);
      const checkAll = await page
        .locator("label")
        .filter({ hasText: "Valitse kaikki" })
        .getByRole("checkbox");
      await pressTabUntilFocusOn(page, checkAll);
      await page.keyboard.press("Space");
      await expect(
        page
          .getByRole("listitem")
          .filter({ has: page.getByRole("checkbox", { checked: true }) })
      ).toHaveCount(await page.getByRole("listitem").count());
      await page.keyboard.press("Space");
      await expect(
        page
          .getByRole("listitem")
          .filter({ has: page.getByRole("checkbox", { checked: true }) })
      ).toHaveCount(0);
    });
    test("finds peruskoulut", async ({ page }) => {
      const button = page.getByRole("button", { name: "Oppilaitostyyppi" });
      await pressTabUntilFocusOn(page, button);
      await page.keyboard.press("Space");
      const checkbox = await page
        .getByRole("listitem")
        .filter({ hasText: "Peruskoulut" })
        .getByRole("checkbox");
      await pressTabUntilFocusOn(page, checkbox);
      await page.keyboard.press("Space");
      const searchButton = await page.getByRole("button", { name: "Hae" });
      await pressTabUntilFocusOn(page, searchButton);
      await page.keyboard.press("Space");
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    });
  });
});

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

async function checkRandomNumberofListItems(page: Page) {
  const listItemsCount = await page.getByRole("listitem").count();
  const checkCount = randomIntFromInterval(1, 5);

  for (let i = 0; i < checkCount; i++) {
    const uncheckedItems = await page.getByRole("listitem").filter({
      has: page.getByRole("checkbox", { checked: false }),
    });
    const count = await uncheckedItems.count();
    const selectedItem = await uncheckedItems
      .nth(randomIntFromInterval(1, count))
      .getByRole("checkbox");
    await pressTabUntilFocusOn(page, selectedItem);
    await page.keyboard.press("Space");
  }
}

function randomIntFromInterval(min, max) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}
