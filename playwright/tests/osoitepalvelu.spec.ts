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
    await expect(page).toHaveTitle(/Osoitepalvelu/);
  });

  test("allows searching for koulutustoimijat", async ({ page }) => {
    await expect(page.getByRole("button", { name: "Hae" })).toBeVisible();
    await page.getByRole("button", { name: "Hae" }).click()

    await expect(page.getByText("4 hakutulosta valittu")).toBeVisible();
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

      const assertSelectionText = async text =>
        expect(button.locator("[aria-live=off]")).toHaveText(text)

      await toggleCheckboxByText(page, "Kesäyliopistot")
      await assertSelectionText("Kesäyliopistot")

      await toggleCheckboxByText(page, "Lukiot")
      await assertSelectionText("Kesäyliopistot, Lukiot")

      await toggleCheckboxByText(page, "Perus- ja lukioasteen koulut")
      await assertSelectionText("Lukiokoulutus, Kesäyliopistot")

      await toggleCheckboxByText(page, "Lukiokoulutus")
      await assertSelectionText("Kesäyliopistot")
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
    const assertSelectionText = async text =>
      expect(button.locator("[aria-live=off]")).toHaveText(text)

    await test.step("Defaults to Manner-Suomi", async () => {
      await assertSelectionText("Manner-Suomi (ei Ahvenanmaa)")
    })

    await button.click();

    await test.step("Changes filter description on selection change", async () => {
      await openDropdown(page, "Hae alueen tai maakunnan nimellä");

      await selectFromDropdown(page, "Koko Suomi");
      await assertSelectionText("Koko Suomi")

      await selectFromAlueDropdown(page, "Ahvenanmaa");
      await assertSelectionText("Manner-Suomi (ei Ahvenanmaa)")

      await selectFromAlueDropdown(page, "Manner-Suomi (ei Ahvenanmaa)");
      await assertSelectionText("")

      await selectFromAlueDropdown(page, "Ulkomaa");
      await assertSelectionText("Ulkomaa")

      await selectFromAlueDropdown(page, "Uusimaa");
      await assertSelectionText("Ulkomaa, Uusimaa")

      await selectFromKuntaDropdown(page, "Imatra");
      await assertSelectionText("Ulkomaa, Uusimaa, Imatra")

      await selectFromKuntaDropdown(page, "Helsinki");
      await assertSelectionText("Ulkomaa, Uusimaa, Helsinki, Imatra")

      await selectFromKuntaDropdown(page, "Helsinki");
      await selectFromKuntaDropdown(page, "Imatra");
      await assertSelectionText("Ulkomaa, Uusimaa")
    })

    await test.step("Filters results by alue", async () => {
      await assertSelectionText("Ulkomaa, Uusimaa")
      await page.getByRole("button", { name: "Hae" }).click()
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await page.getByRole("button", { name: "Muokkaa hakua" }).click();

      await openSijaintiBox(page)
      await selectFromAlueDropdown(page, "Uusimaa");
      await selectFromAlueDropdown(page, "Etelä-Karjala"); // Sisältää Imatran
      await page.getByRole("button", { name: "Hae" }).click()
      await expect(page.getByText("3 hakutulosta valittu")).toBeVisible();
      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult).toBeVisible();
      await expect(firstResult.getByText("Mansikkalan testi kunta")).toBeVisible();
      await expect(firstResult.getByText("Imatra").first()).toBeVisible();
    })
  })

  test("Järjestämislupa filter", async({ page }) => {
    const button = page.getByRole("button", { name: "Ammatillisen koulutuksen järjestämislupa" });
    const assertSelectionText = async text =>
      expect(button.locator("[aria-live=off]")).toHaveText(text);

    await button.click();

    await test.step("Specific koulutuslupa", async () => {
      await selectFromJärjestämislupaDropdown(page, "Hieronnan ammattitutkinto");
      await assertSelectionText("Hieronnan ammattitutkinto");

      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult.getByText("Helsingin kaupunki")).toBeVisible();
    });

    await page.getByRole("button", { name: "Muokkaa hakua" }).click();
    await button.click();

    await test.step("Any koulutuslupa", async () => {
      await toggleCheckboxByText(page, "Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa");
      await assertSelectionText("Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa")

      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();

      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult.getByText("Helsingin kaupunki")).toBeVisible();
    });

  });
});

async function selectFromJärjestämislupaDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!await page.isVisible('.jarjesetamislupa-react-select__menu')) {
    await openDropdown(page, "Hae yksittäisten tutkintojen ja koulutusten nimillä");
  }
  await page.getByLabel(label, { exact: true }).click()
}

async function selectFromAlueDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!await page.isVisible('.alue-react-select__menu')) {
    await openDropdown(page, "Hae alueen tai maakunnan nimellä");
  }
  await page.getByLabel(label, { exact: true }).click()
}

async function selectFromKuntaDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!await page.isVisible('.kunta-react-select__menu')) {
    await openDropdown(page, "Hae kunnan nimellä");
  }
  await page.getByLabel(label, { exact: true }).click()
}
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

async function openSijaintiBox(page: Page) {
  const button = page.getByRole("button", { name: "Sijainti" });
  await pressTabUntilFocusOn(page, button);
  await page.keyboard.press("Space");

  return button;
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
