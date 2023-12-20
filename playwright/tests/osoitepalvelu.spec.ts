import { expect, Locator, Page, test } from "@playwright/test";
import { OsoitepalveluPage } from "./OsoitepalveluPage";

test.describe("Osoitepalvelu", () => {
  test.beforeAll(async ({ request }, testInfo) => {
    await test.step("Load initial mock data", async () => {
      await request.post(
        "http://localhost:3003/organisaatio-service/mock/init"
      );
    });
  });
  test.beforeEach(async ({ page }, testInfo) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    await osoitepalveluPage.goto();
  });

  test("is in the initial state when openened", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);

    await test.step("the page has a title", async () => {
      await expect(page).toHaveTitle(/Osoitepalvelu/);
    });
    await test.step("Kohderyhmä Koulutustoimijat is selected", async () => {
      await expect(page.getByLabel("Koulutustoimijat")).toBeChecked();
    });
    await test.step("instructions are displayed", async () => {
      await expect(page.getByText("Hae*")).toBeVisible();
      await expect(page.getByText("Haun rajausmahdollisuudet")).toBeVisible();
    });
    await test.step("advanced search filters are enabled", async () => {
      await expect(
        osoitepalveluPage.oppilaitostyyppiFilter.scope
      ).toBeEnabled();
      await expect(osoitepalveluPage.sijaintiFilter.scope).toBeEnabled();
      await expect(osoitepalveluPage.jarjestamislupaFilter.scope).toBeEnabled();
      await expect(osoitepalveluPage.kieliFilter.scope).toBeEnabled();
    });
    await test.step("Buttons are enabled", async () => {
      await expect(osoitepalveluPage.haeButton).toBeEnabled();
      await expect(osoitepalveluPage.tyhjennaButton).toBeEnabled();
    });
  });

  test("allows searching for koulutustoimijat", async ({ page }) => {
    await new OsoitepalveluPage(page).kieliFilter.clear();
    await expect(page.getByRole("button", { name: "Hae" })).toBeVisible();
    await page.getByRole("button", { name: "Hae" }).click();

    await expect(page.getByText("4 hakutulosta valittu")).toBeVisible();
    await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    await expect(
      page.getByText("Testi Koulutuskuntayhtymä Puolukka")
    ).toBeVisible();
    await expect(page.getByText("Mustikkalan testi yhdistys")).toBeVisible();
  });

  test("retains search parameters when going back from search results", async ({
    page,
  }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;

    await osoitepalveluPage.kieliFilter.clear();
    await oppilaitostyyppiFilter.open();
    await oppilaitostyyppiFilter.toggleCheckboxByLabel("Peruskoulut");
    await oppilaitostyyppiFilter.toggleCheckboxByLabel("Ammattikorkeakoulut");
    await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
      "Ammattikorkeakoulut, Peruskoulut"
    );
    await page.getByRole("button", { name: "Hae" }).click();
    await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
    await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    await page.getByRole("button", { name: "Muokkaa hakua" }).click();

    await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
      "Ammattikorkeakoulut, Peruskoulut"
    );
  });

  test.describe("Oppilaitostyyppi filter", () => {
    test("opens and closes", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;

      await expect(oppilaitostyyppiFilter.contents).toBeHidden();
      await oppilaitostyyppiFilter.open();
      await expect(oppilaitostyyppiFilter.contents).toBeVisible();
      await oppilaitostyyppiFilter.close();
      await expect(oppilaitostyyppiFilter.contents).toBeHidden();
    });

    test("updates selection text", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;
      await oppilaitostyyppiFilter.open();

      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Kesäyliopistot");
      await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
        "Kesäyliopistot"
      );

      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Lukiot");
      await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
        "Kesäyliopistot, Lukiot"
      );

      await oppilaitostyyppiFilter.toggleCheckboxByLabel(
        "Perus- ja lukioasteen koulut"
      );
      await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
        "Lukiokoulutus, Kesäyliopistot"
      );

      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Lukiokoulutus");
      await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
        "Kesäyliopistot"
      );
    });

    test("Tyhjennä valinnat unchecks all selections", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;
      const clearButton = page.getByRole("button", {
        name: "Tyhjennä valinnat",
      });

      await oppilaitostyyppiFilter.open();
      await expect(getCheckedItems(page)).toHaveCount(0);
      await expect(clearButton).toBeDisabled();
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Ammattikorkeakoulut");
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Peruskoulut");
      await expect(getCheckedItems(page)).toHaveCount(2);
      await expect(clearButton).toBeEnabled();
      await page.getByRole("button", { name: "Tyhjennä valinnat" }).click();
      await expect(getCheckedItems(page)).toHaveCount(0);
      await expect(clearButton).toBeDisabled();
    });

    test("checks and unchecks all selections", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;

      await oppilaitostyyppiFilter.open();
      await expect(getCheckedItems(page)).toHaveCount(0);
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Valitse kaikki");
      await expect(getCheckedItems(page)).toHaveCount(
        await page.getByRole("listitem").count()
      );
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Valitse kaikki");
      await expect(getCheckedItems(page)).toHaveCount(0);
    });
    test("finds peruskoulut", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;

      await oppilaitostyyppiFilter.open();
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Peruskoulut");
      const searchButton = await page.getByRole("button", { name: "Hae" });
      await pressTabUntilFocusOn(page, searchButton);
      await page.keyboard.press("Space");
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
    });

    test("finds peruskoulut and filters by vuosiluokka", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;
      await oppilaitostyyppiFilter.open();

      const vuosiluokatInput = await page.getByLabel(
        "Hae perusopetuksen vuosiluokkatiedolla"
      );
      await test.step(
        "vuosiluokka selection is disabled if no peruskoulut is selected",
        async () => {
          await expect(vuosiluokatInput).toBeDisabled();
        }
      );
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Peruskoulut");
      await test.step(
        "vuosiluokka selection is enabled after selecting peruskoulut",
        async () => {
          await expect(vuosiluokatInput).toBeEnabled();
        }
      );

      await openDropdown(page, "Hae perusopetuksen vuosiluokkatiedolla");
      await selectFromDropdown(page, "Lisäopetuksessa");
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("0 hakutulosta valittu")).toBeVisible();
    });

    test("checks and unchecks all oppilaitostyyppi that are part of a group", async ({
      page,
    }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;

      await oppilaitostyyppiFilter.open();
      await expect(getCheckedItems(page)).toHaveCount(0);
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Korkeakoulutus");
      await expect(getCheckedItems(page)).toHaveText([
        "Ammattikorkeakoulut",
        "Sotilaskorkeakoulut",
        "Yliopistot",
      ]);
      await test.step(
        "selection description shows group name instead of individiaul types",
        async () => {
          await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
            "Korkeakoulutus"
          );
        }
      );
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Korkeakoulutus");
      await expect(getCheckedItems(page)).toHaveCount(0);
    });

    test("checks and unchecks an oppilatostyyppi group", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;

      await oppilaitostyyppiFilter.open();
      await expect(getCheckedItems(page)).toHaveCount(0);
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Ammattikorkeakoulut");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).not.toBeChecked();
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Sotilaskorkeakoulut");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).not.toBeChecked();
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Yliopistot");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).toBeChecked();
      await oppilaitostyyppiFilter.toggleCheckboxByLabel("Sotilaskorkeakoulut");
      await expect(getCheckboxByText(page, "Korkeakoulutus")).not.toBeChecked();
    });
  });

  test("Sijainti filter", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const sijaintiFilter = osoitepalveluPage.sijaintiFilter;

    await osoitepalveluPage.kieliFilter.clear();

    await test.step("Defaults to Manner-Suomi", async () => {
      await expect(sijaintiFilter.selectionIndicator).toHaveText(
        "Manner-Suomi (ei Ahvenanmaa)"
      );
    });

    await sijaintiFilter.open();

    await test.step(
      "Changes filter description on selection change",
      async () => {
        await openDropdown(page, "Hae alueen tai maakunnan nimellä");

        await selectFromDropdown(page, "Koko Suomi");
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Koko Suomi"
        );

        await selectFromAlueDropdown(page, "Ahvenanmaa");
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Manner-Suomi (ei Ahvenanmaa)"
        );

        await selectFromAlueDropdown(page, "Manner-Suomi (ei Ahvenanmaa)");
        await expect(sijaintiFilter.selectionIndicator).toHaveText("");

        await selectFromAlueDropdown(page, "Ulkomaa");
        await expect(sijaintiFilter.selectionIndicator).toHaveText("Ulkomaa");

        await selectFromAlueDropdown(page, "Uusimaa");
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Ulkomaa, Uusimaa"
        );

        await selectFromKuntaDropdown(page, "Imatra");
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Ulkomaa, Uusimaa, Imatra"
        );

        await selectFromKuntaDropdown(page, "Helsinki");
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Ulkomaa, Uusimaa, Helsinki, Imatra"
        );

        await selectFromKuntaDropdown(page, "Helsinki");
        await selectFromKuntaDropdown(page, "Imatra");
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Ulkomaa, Uusimaa"
        );
      }
    );

    await test.step("Filters results by alue", async () => {
      await expect(sijaintiFilter.selectionIndicator).toHaveText(
        "Ulkomaa, Uusimaa"
      );
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await page.getByRole("button", { name: "Muokkaa hakua" }).click();

      await selectFromAlueDropdown(page, "Uusimaa");
      await selectFromAlueDropdown(page, "Etelä-Karjala"); // Sisältää Imatran
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("3 hakutulosta valittu")).toBeVisible();
      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult).toBeVisible();
      await expect(
        firstResult.getByText("Mansikkalan testi kunta")
      ).toBeVisible();
      await expect(firstResult.getByText("Imatra").first()).toBeVisible();
    });
  });

  test("Järjestämislupa filter", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const jarjestamislupaFilter = osoitepalveluPage.jarjestamislupaFilter;

    await osoitepalveluPage.kieliFilter.clear();
    await jarjestamislupaFilter.open();

    await test.step("Specific koulutuslupa", async () => {
      await selectFromJärjestämislupaDropdown(
        page,
        "Hieronnan ammattitutkinto"
      );
      await expect(jarjestamislupaFilter.selectionIndicator).toHaveText(
        "Hieronnan ammattitutkinto"
      );

      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult.getByText("Helsingin kaupunki")).toBeVisible();
    });

    await page.getByRole("button", { name: "Muokkaa hakua" }).click();

    await test.step("Any koulutuslupa", async () => {
      await jarjestamislupaFilter.toggleCheckboxByLabel(
        "Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa"
      );
      await expect(jarjestamislupaFilter.selectionIndicator).toHaveText(
        "Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa"
      );

      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();

      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult.getByText("Helsingin kaupunki")).toBeVisible();
    });
  });

  test("Oppilaitoksen kieli filter", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const kieliFilter = osoitepalveluPage.kieliFilter;

    await osoitepalveluPage.kieliFilter.clear();
    await kieliFilter.open();

    await test.step("No filter searches all", async () => {
      await expect(kieliFilter.selectionIndicator).toHaveText("");
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("4 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Helsingin kaupunki")).toBeVisible();
      await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
      await expect(page.getByText("Mustikkalan testi yhdistys")).toBeVisible();
      await expect(
        page.getByText("Testi Koulutuskuntayhtymä Puolukka")
      ).toBeVisible();
    });

    await page.getByRole("button", { name: "Muokkaa hakua" }).click();

    await test.step("suomi and ruotsi finds testiorganisaatiot", async () => {
      await kieliFilter.toggleCheckboxByLabel("suomi");
      await kieliFilter.toggleCheckboxByLabel("ruotsi");
      await expect(kieliFilter.selectionIndicator).toHaveText("suomi, ruotsi");

      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("3 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
      await expect(page.getByText("Mustikkalan testi yhdistys")).toBeVisible();
      await expect(
        page.getByText("Testi Koulutuskuntayhtymä Puolukka")
      ).toBeVisible();
    });

    await page.getByRole("button", { name: "Muokkaa hakua" }).click();

    await test.step("ruotsi finds none", async () => {
      await kieliFilter.toggleCheckboxByLabel("suomi");
      await expect(kieliFilter.selectionIndicator).toHaveText("ruotsi");
      await page.getByRole("button", { name: "Hae" }).click();
      await expect(page.getByText("0 hakutulosta valittu")).toBeVisible();
    });
  });

  test("Lataa Excel button", async ({ page }) => {
    await test.step(
      "downloads search results as an excel document",
      async () => {
        await page.getByRole("button", { name: "Hae" }).click();
        const [download] = await Promise.all([
          page.waitForEvent("download"),
          page.getByRole("button", { name: "Lataa Excel" }).click(),
        ]);
        expect(download.suggestedFilename()).toBe("osoitteet.xls");
      }
    );
  });
});

async function selectFromJärjestämislupaDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!(await page.isVisible(".jarjesetamislupa-react-select__menu"))) {
    await openDropdown(
      page,
      "Hae yksittäisten tutkintojen ja koulutusten nimillä"
    );
  }
  await page.getByLabel(label, { exact: true }).click();
}

async function selectFromAlueDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!(await page.isVisible(".alue-react-select__menu"))) {
    await openDropdown(page, "Hae alueen tai maakunnan nimellä");
  }
  await page.getByLabel(label, { exact: true }).click();
}

async function selectFromKuntaDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!(await page.isVisible(".kunta-react-select__menu"))) {
    await openDropdown(page, "Hae kunnan nimellä");
  }
  await page.getByLabel(label, { exact: true }).click();
}
async function openDropdown(page: Page, label: string) {
  await page.getByText(label, { exact: true }).click();
}

async function selectFromDropdown(page: Page, label: string) {
  await page.getByLabel(label, { exact: true }).click();
}

function getCheckboxByText(page: Page, name: string) {
  return page.getByLabel(name, { exact: true });
}

function getCheckedItems(page: Page) {
  return getItemsByCheckState(page, true);
}

function getItemsByCheckState(page: Page, checked: boolean) {
  return page.getByRole("listitem").filter({
    has: page.getByRole("checkbox", { checked }),
  });
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
