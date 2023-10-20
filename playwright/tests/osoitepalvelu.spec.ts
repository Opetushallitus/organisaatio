import { expect, Locator, Page, test } from "@playwright/test";

test.describe("Osoitepalvelu", () => {
  test.beforeAll(async ({ request }, testInfo) => {
    await test.step("Load initial mock data", async () => {
      await request.post("http://localhost:3003/organisaatio-service/mock/init");
    });
  });
  test.beforeEach(async ({ page }, testInfo) => {
    await page.goto("http://localhost:3003/osoitteet");
  });

  test("has title", async ({ page }) => {
    await expect(page).toHaveTitle(/Organisaatio/);
  });
  test("allows searching for koulutustoimijat", async ({ page }) => {
    await expect(page.getByRole("button", { name: "Hae" })).toBeVisible();
    await page.getByRole("button", { name: "Hae" }).click()

    await expect(page.getByText("3 hakutulosta valittu")).toBeVisible();
    await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    await expect(
      page.getByText("Testi Koulutuskuntayhtymä Puolukka")
    ).toBeVisible();
    await expect(page.getByText("Mustikkalan testi yhdistys")).toBeVisible();
  });

  test("retains search parameters when going back from search results", async ({ page }) => {
      const button = await openOppilatostyyppiBoxAndReturnOpeningButton(page);
      await toggleCheckboxByText(page, "Peruskoulut");
      await toggleCheckboxByText(page, "Ammattikorkeakoulut");
      await expect(button.locator("[aria-live=off]")).toHaveText("Ammattikorkeakoulut, Peruskoulut");
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
      await page.getByRole("button", { name: "Muokkaa hakua" }).click();

      await expect(button.locator("[aria-live=off]")).toHaveText("Ammattikorkeakoulut, Peruskoulut");
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
      const button = await openOppilatostyyppiBoxAndReturnOpeningButton(page);
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

    test("Tyhjennä valinnat unchecks all selections", async ({page}) => {
      const clearButton = page.getByRole("button", {name: "Tyhjennä valinnat"})
      await openOppilatostyyppiBox(page);
      await expect(getCheckedItems(page)).toHaveCount(0);
      await expect(clearButton).toBeDisabled();
      await toggleCheckboxByText(page, "Ammattikorkeakoulut")
      await toggleCheckboxByText(page, "Peruskoulut")
      await expect(getCheckedItems(page)).toHaveCount(2);
      await expect(clearButton).toBeEnabled();
      await page.getByRole("button", {name: "Tyhjennä valinnat"}).click();
      await expect(getCheckedItems(page)).toHaveCount(0);
      await expect(clearButton).toBeDisabled();
    });

    test("checks and unchecks all selections", async ({ page }) => {
      await openOppilatostyyppiBox(page);
      await expect(getCheckedItems(page)).toHaveCount(0);
      await toggleCheckboxByText(page, "Valitse kaikki");
      await expect(getCheckedItems(page)).toHaveCount(
        await page.getByRole("listitem").count()
      );
      await toggleCheckboxByText(page, "Valitse kaikki");
      await expect(getCheckedItems(page)).toHaveCount(0);
    });
    test("finds peruskoulut", async ({ page }) => {
      await openOppilatostyyppiBox(page);
      await toggleCheckboxByText(page, "Peruskoulut");
      const searchButton = await page.getByRole("button", { name: "Hae" });
      await pressTabUntilFocusOn(page, searchButton);
      await page.keyboard.press("Space");
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    });

    test("finds peruskoulut and filters by vuosiluokka", async ({ page }) => {
      await openOppilatostyyppiBox(page);

      const vuosiluokatInput = await page.getByLabel("Hae perusopetuksen vuosiluokkatiedolla");
      await test.step("vuosiluokka selection is disabled if no peruskoulut is selected", async () => {
        await expect(vuosiluokatInput).toBeDisabled();
      });
      await toggleCheckboxByText(page, "Peruskoulut");
      await test.step("vuosiluokka selection is enabled after selecting peruskoulut", async () => {
        await expect(vuosiluokatInput).toBeEnabled();
      });

      await openDropdown(page, "Hae perusopetuksen vuosiluokkatiedolla")
      await selectFromDropdown(page, "Lisäopetuksessa");
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("0 hakutulosta valittu")).toBeVisible();
    });

    test("checks and unchecks all oppilaitostyyppi that are part of a group", async ({
      page,
    }) => {
      const button = await openOppilatostyyppiBoxAndReturnOpeningButton(page);
      await expect(getCheckedItems(page)).toHaveCount(0);
      await toggleCheckboxByText(page, "Korkeakoulutus");
      await expect(getCheckedItems(page)).toHaveText([
        "Ammattikorkeakoulut",
        "Sotilaskorkeakoulut",
        "Yliopistot",
      ]);
      await test.step("selection description shows group name instead of individiaul types", async () => {
        await expect(button.locator("[aria-live=off]")).toHaveText("Korkeakoulutus");
      });
      await toggleCheckboxByText(page, "Korkeakoulutus");
      await expect(getCheckedItems(page)).toHaveCount(0);
    });
    test("checks and unchecks an oppilatostyyppi group", async ({ page }) => {
      await openOppilatostyyppiBox(page);
      await expect(getCheckedItems(page)).toHaveCount(0);
      await toggleCheckboxByText(page, "Ammattikorkeakoulut");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).not.toBeChecked();
      await toggleCheckboxByText(page, "Sotilaskorkeakoulut");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).not.toBeChecked();
      await toggleCheckboxByText(page, "Yliopistot");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).toBeChecked();
      await toggleCheckboxByText(page, "Sotilaskorkeakoulut");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).not.toBeChecked();
    });
  });

  test("Sijainti filter", async ({ page }) => {
    const button = page.getByRole("button", { name: "Sijainti" });

    await test.step("Defaults to Manner-Suomi", async () => {
      await expect(button.locator("[aria-live=off]")).toHaveText("Manner-Suomi (ei Ahvenanmaa)");
    })

    await button.click();

    await test.step("Changes filter description on selection change", async () => {
      await openDropdown(page, "Hae alueen tai maakunnan nimellä");

      await selectFromDropdown(page, "Koko Suomi");
      await expect(button.locator("[aria-live=off]")).toHaveText("Koko Suomi");

      await selectFromDropdown(page, "Ahvenanmaa");
      await expect(button.locator("[aria-live=off]")).toHaveText("Manner-Suomi (ei Ahvenanmaa)");

      await selectFromDropdown(page, "Manner-Suomi (ei Ahvenanmaa)");
      await expect(button.locator("[aria-live=off]")).toHaveText("");

      await selectFromDropdown(page, "Ulkomaa");
      await expect(button.locator("[aria-live=off]")).toHaveText("Ulkomaa");

      await selectFromDropdown(page, "Uusimaa");
      await expect(button.locator("[aria-live=off]")).toHaveText("Ulkomaa, Uusimaa");
    })
  })
});

async function openDropdown(page: Page, label: string) {
  await page.getByText(label, { exact: true }).click();
}

async function selectFromDropdown(page: Page, label: string) {
  await page.getByLabel(label, { exact: true }).click()
}

async function toggleCheckboxByText(page: Page, name: string) {
  const checkbox = await getCheckboxByText(page, name);
  await pressTabUntilFocusOn(page, checkbox);
  await page.keyboard.press("Space");
}

function getCheckboxByText(page: Page, name: string) {
  return page.getByLabel(name, { exact: true });
}

function getCheckedItems(page: Page) {
  return getItemsByCheckState(page, true);
}

function getUncheckedItems(page: Page) {
  return getItemsByCheckState(page, false);
}

function getItemsByCheckState(page: Page, checked: boolean) {
  return page.getByRole("listitem").filter({
    has: page.getByRole("checkbox", { checked }),
  });
}

async function openOppilatostyyppiBox(page: Page) {
  await openOppilatostyyppiBoxAndReturnOpeningButton(page);
}

async function openOppilatostyyppiBoxAndReturnOpeningButton(page: Page) {
  const button = page.getByRole("button", { name: "Oppilaitostyyppi" });
  await pressTabUntilFocusOn(page, button);
  await page.keyboard.press("Space");

  return button;
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

async function checkRandomNumberofListItems(page: Page) {
  const listItemsCount = await page.getByRole("listitem").count();
  const checkCount = randomIntFromInterval(1, 5);

  for (let i = 0; i < checkCount; i++) {
    const uncheckedItems = await getUncheckedItems(page);
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
