import path from "path";
import {
  APIRequestContext,
  expect,
  Locator,
  Page,
  test,
} from "@playwright/test";
import { FormField, OsoitepalveluPage } from "./OsoitepalveluPage";

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
      await expect(osoitepalveluPage.koulutustoimijatCheckbox).toBeChecked();
      await expect(
        osoitepalveluPage.palveluidenKayttajatKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.varhaiskasvatustoimijatKohderyhma.checkbox
      ).toBeDisabled();
    });
    await test.step("instructions are displayed", async () => {
      await expect(osoitepalveluPage.kohderyhmaInstructions).toHaveText("Hae*");
      await expect(osoitepalveluPage.filterInstructions).toHaveText(
        "Haun rajausmahdollisuudet"
      );
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

  test("tyhjennä button", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);

    await osoitepalveluPage.oppilaitostyyppiFilter.button.click();
    await osoitepalveluPage.jarjestamislupaFilter.button.click();
    await osoitepalveluPage.sijaintiFilter.button.click();
    await osoitepalveluPage.kieliFilter.button.click();
    await expect(
      osoitepalveluPage.oppilaitostyyppiFilter.contents
    ).toBeVisible();
    await expect(
      osoitepalveluPage.jarjestamislupaFilter.contents
    ).toBeVisible();
    await expect(osoitepalveluPage.sijaintiFilter.contents).toBeVisible();
    await expect(osoitepalveluPage.kieliFilter.contents).toBeVisible();

    await osoitepalveluPage.tyhjennaButton.click();

    await test.step("does not change the instructions", async () => {
      await expect(osoitepalveluPage.kohderyhmaInstructions).toHaveText("Hae*");
      await expect(osoitepalveluPage.filterInstructions).toHaveText(
        "Haun rajausmahdollisuudet"
      );
    });

    await test.step("clears Kohderyhmä selection", async () => {
      await expect(
        osoitepalveluPage.koulutustoimijatCheckbox
      ).not.toBeChecked();
    });

    await test.step("clears all filters", async () => {
      await expect(
        osoitepalveluPage.oppilaitostyyppiFilter.checkedCheckboxLabels
      ).toHaveCount(0);
      await expect(
        osoitepalveluPage.oppilaitostyyppiFilter.selectionIndicator
      ).toHaveText("");

      await expect(
        osoitepalveluPage.jarjestamislupaFilter.checkedCheckboxLabels
      ).toHaveCount(0);
      await expect(
        osoitepalveluPage.jarjestamislupaFilter.selectionIndicator
      ).toHaveText("");

      await expect(
        osoitepalveluPage.sijaintiFilter.checkedCheckboxLabels
      ).toHaveCount(0);
      await expect(
        osoitepalveluPage.sijaintiFilter.selectionIndicator
      ).toHaveText("");

      await expect(
        osoitepalveluPage.kieliFilter.checkedCheckboxLabels
      ).toHaveCount(0);
      await expect(osoitepalveluPage.kieliFilter.selectionIndicator).toHaveText(
        ""
      );
    });

    await test.step("disables all filters", async () => {
      await expect(
        osoitepalveluPage.oppilaitostyyppiFilter.scope
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.jarjestamislupaFilter.scope
      ).toBeDisabled();
      await expect(osoitepalveluPage.sijaintiFilter.scope).toBeDisabled();
      await expect(osoitepalveluPage.kieliFilter.scope).toBeDisabled();
    });

    await test.step("closes all filters", async () => {
      await expect(
        osoitepalveluPage.oppilaitostyyppiFilter.contents
      ).toBeHidden();
      await expect(
        osoitepalveluPage.jarjestamislupaFilter.contents
      ).toBeHidden();
      await expect(osoitepalveluPage.sijaintiFilter.contents).toBeHidden();
      await expect(osoitepalveluPage.kieliFilter.contents).toBeHidden();
    });
  });

  test("allows searching for koulutustoimijat", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);

    await osoitepalveluPage.kieliFilter.clear();
    await osoitepalveluPage.haeButton.click();

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
    await osoitepalveluPage.haeButton.click();
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
      await osoitepalveluPage.haeButton.click();
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
      await osoitepalveluPage.haeButton.click();
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

    await test.step("Defaults to Manner-Suomi", async () => {
      await expect(sijaintiFilter.selectionIndicator).toHaveText(
        "Manner-Suomi (ei Ahvenanmaa)"
      );
    });

    await test.step(
      "reverts to default value after being re-enabled",
      async () => {
        await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
        await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
        await expect(sijaintiFilter.selectionIndicator).toHaveText(
          "Manner-Suomi (ei Ahvenanmaa)"
        );
      }
    );

    await osoitepalveluPage.kieliFilter.clear();
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
      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await page.getByRole("button", { name: "Muokkaa hakua" }).click();

      await selectFromAlueDropdown(page, "Uusimaa");
      await selectFromAlueDropdown(page, "Etelä-Karjala"); // Sisältää Imatran
      await osoitepalveluPage.haeButton.click();
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

      await osoitepalveluPage.haeButton.click();
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

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();

      const firstResult = await page.getByRole("row").nth(1);
      await expect(firstResult.getByText("Helsingin kaupunki")).toBeVisible();
    });
  });

  test("Oppilaitoksen kieli filter", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const kieliFilter = osoitepalveluPage.kieliFilter;

    await test.step("defaults to suomi", async () => {
      await expect(kieliFilter.selectionIndicator).toHaveText("suomi");
    });

    await test.step(
      "reverts to default value after being re-enabled",
      async () => {
        await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
        await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
        await expect(kieliFilter.selectionIndicator).toHaveText("suomi");
      }
    );

    await osoitepalveluPage.kieliFilter.clear();
    await kieliFilter.open();

    await test.step("No filter searches all", async () => {
      await expect(kieliFilter.selectionIndicator).toHaveText("");
      await osoitepalveluPage.haeButton.click();
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

      await osoitepalveluPage.haeButton.click();
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
      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("0 hakutulosta valittu")).toBeVisible();
    });
  });

  test.describe("Lataa Excel button", () => {
    test("downloads organisation search results as an excel document", async ({
      page,
    }) => {
      await new OsoitepalveluPage(page).haeButton.click();
      const [download] = await Promise.all([
        page.waitForEvent("download"),
        page.getByRole("button", { name: "Lataa Excel" }).click(),
      ]);
      expect(download.suggestedFilename()).toBe("osoitteet.xls");
    });

    test("downloads kayttaja search results as an excel document", async ({
      page,
    }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
      await osoitepalveluPage.palveluidenKayttajatKohderyhma.toggle();
      await osoitepalveluPage.kayttajaFilter.open();
      await selectFromKoulutustoimijaDropdown(
        page,
        "Testi Koulutuskuntayhtymä Puolukka"
      );
      await osoitepalveluPage.haeButton.click();
      const [download] = await Promise.all([
        page.waitForEvent("download"),
        page.getByRole("button", { name: "Lataa Excel" }).click(),
      ]);
      expect(download.suggestedFilename()).toBe("kayttajat.xls");
    });
  });

  test.describe("Oppilaitokset kohderyhmä", async () => {
    test.beforeEach(async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
      await osoitepalveluPage.oppilaitoksetKohderyhma.toggle();
    });

    test("disables other kohderyhmas", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await expect(
        osoitepalveluPage.palveluidenKayttajatKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.varhaiskasvatustoimijatKohderyhma.checkbox
      ).toBeDisabled();
    });

    test("filters results by oppilaitostyyppi", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.oppilaitostyyppiFilter.open();
      await osoitepalveluPage.oppilaitostyyppiFilter.toggleCheckboxByLabel(
        "Perusopetus"
      );
      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(
        page.getByText("Mansikkalan testi peruskoulu")
      ).toBeVisible();
    });
  });

  test.describe("Oppilaitosten toimipisteet kohderyhmä", async () => {
    test.beforeEach(async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
      await osoitepalveluPage.oppilaitostentoimipisteetKohderyhma.toggle();
    });

    test("disables other kohderyhmas", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await expect(
        osoitepalveluPage.palveluidenKayttajatKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.varhaiskasvatustoimijatKohderyhma.checkbox
      ).toBeDisabled();
    });

    test("shows only oppilaitosten toimipisteet", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(
        page.getByText("Ammattiopisto Puolukka, testi toimipiste")
      ).toBeVisible();
    });

    test("filters by parent oppilaitostyyppi", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.oppilaitostyyppiFilter.open();
      await osoitepalveluPage.oppilaitostyyppiFilter.toggleCheckboxByLabel(
        "Ammatillinen koulutus"
      );

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(
        page.getByText("Ammattiopisto Puolukka, testi toimipiste")
      ).toBeVisible();

      await page.getByRole("button", { name: "Muokkaa hakua" }).click();
      await osoitepalveluPage.oppilaitostyyppiFilter.toggleCheckboxByLabel(
        "Ammatillinen koulutus"
      );
      await osoitepalveluPage.oppilaitostyyppiFilter.toggleCheckboxByLabel(
        "Perusopetus"
      );

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("0 hakutulosta valittu")).toBeVisible();
    });
  });

  test.describe("Varhaiskasvatustoimijat kohderyhmä", async () => {
    test.beforeEach(async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
      await osoitepalveluPage.varhaiskasvatustoimijatKohderyhma.toggle();
    });

    test("disables other kohderyhmas", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await expect(
        osoitepalveluPage.koulutusotimijatKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.oppilaitoksetKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.oppilaitostentoimipisteetKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.palveluidenKayttajatKohderyhma.checkbox
      ).toBeDisabled();
    });

    test("filters results by varhaiskasvatustoimija", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("2 hakutulosta valittu")).toBeVisible();
      await expect(
        page.getByText("Varhaiskasvatuksen järjestäjä kunta testi")
      ).toBeVisible();
      await expect(
        page.getByText("Varhaiskasvatuksen järjestäjä yksityinen")
      ).toBeVisible();
    });
  });

  test.describe("Palveluiden käyttäjät kohderyhmä", async () => {
    test.beforeEach(async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
      await osoitepalveluPage.palveluidenKayttajatKohderyhma.toggle();
    });

    test("disables other kohderyhmas", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await expect(
        osoitepalveluPage.koulutusotimijatKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.oppilaitoksetKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.oppilaitostentoimipisteetKohderyhma.checkbox
      ).toBeDisabled();
      await expect(
        osoitepalveluPage.varhaiskasvatustoimijatKohderyhma.checkbox
      ).toBeDisabled();
    });

    test("is disabled without filters", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await expect(osoitepalveluPage.haeButton).toBeDisabled();
    });

    test("filters by oppilaitostyyppi", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.oppilaitostyyppiFilter.open();
      await osoitepalveluPage.oppilaitostyyppiFilter.toggleCheckboxByLabel(
        "Perusopetus"
      );

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Faija Mehiläinen")).toBeVisible();
    });

    test("filters by koulutustoimija", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.kayttajaFilter.open();
      await selectFromKoulutustoimijaDropdown(
        page,
        "Testi Koulutuskuntayhtymä Puolukka"
      );

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("2 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Ville Valtionavustus")).toBeVisible();
      await expect(page.getByText("Matti Meikäläinen")).toBeVisible();
    });

    test("filters by käyttöoikeusryhmä", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.kayttajaFilter.open();
      await selectFromKayttooikeusryhmaDropdown(page, "Puolakan kiertäjä");

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("2 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Ville Valtionavustus")).toBeVisible();
      await expect(page.getByText("Paula Puolukka")).toBeVisible();
    });

    test("filters by käyttöoikeusryhmä and koulutustoimija", async ({
      page,
    }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);

      await osoitepalveluPage.kayttajaFilter.open();
      await selectFromKayttooikeusryhmaDropdown(page, "Puolakan kiertäjä");
      await selectFromKoulutustoimijaDropdown(
        page,
        "Testi Koulutuskuntayhtymä Puolukka"
      );

      await osoitepalveluPage.haeButton.click();
      await expect(page.getByText("1 hakutulosta valittu")).toBeVisible();
      await expect(page.getByText("Ville Valtionavustus")).toBeVisible();
    });
  });

  test.describe("Selecting multiple kohderyhmä", async () => {
    test("keeps filter configurations", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const oppilaitostyyppiFilter = osoitepalveluPage.oppilaitostyyppiFilter;
      const jarjestamislupaFilter = osoitepalveluPage.jarjestamislupaFilter;
      const kieliFilter = osoitepalveluPage.kieliFilter;

      await test.step("change oppilaitostyyppi filter", async () => {
        await oppilaitostyyppiFilter.open();
        await oppilaitostyyppiFilter.toggleCheckboxByLabel("Perusopetus");
        await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
          "Perusopetus"
        );
      });

      await test.step("change järjestämislupa filter", async () => {
        await jarjestamislupaFilter.open();
        await jarjestamislupaFilter.toggleCheckboxByLabel(
          "Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa"
        );
        await jarjestamislupaFilter.close();
        await expect(jarjestamislupaFilter.selectionIndicator).toHaveText(
          "Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa"
        );
      });

      await test.step("change kieli filter", async () => {
        await kieliFilter.open();
        await kieliFilter.toggleCheckboxByLabel("saame");
        await kieliFilter.close();
        await expect(kieliFilter.selectionIndicator).toHaveText("suomi, saame");
      });

      await test.step("toggle kohderyhmä selections", async () => {
        await osoitepalveluPage.oppilaitoksetKohderyhma.toggle();
        await osoitepalveluPage.oppilaitostentoimipisteetKohderyhma.toggle();
        await osoitepalveluPage.oppilaitoksetKohderyhma.toggle();
      });

      await test.step("assertions", async () => {
        await expect(oppilaitostyyppiFilter.selectionIndicator).toHaveText(
          "Perusopetus"
        );
        await expect(jarjestamislupaFilter.selectionIndicator).toHaveText(
          "Kaikki koulutustoimijat, joilla voimassa oleva järjestämislupa"
        );
        await expect(kieliFilter.selectionIndicator).toHaveText("suomi, saame");
      });
    });
  });

  test("Kirjoita viesti form", async ({ page, browserName }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
    const aiheField = kirjoitaViestiForm.aiheField;
    const viestiField = kirjoitaViestiForm.viestiField;

    await osoitepalveluPage.haeButton.click();
    await osoitepalveluPage.kirjoitaSahkopostiButton.click();

    await test.step("number of recipients is shown", async () => {
      await expect(page.getByText("3 vastaanottajaa")).toBeVisible();
    });

    await test.step("has a default state", async () => {
      await expect(aiheField.input).toHaveText("");
      await expect(aiheField.errorFeedback).not.toBeVisible();
      await expect(viestiField.input).toHaveText("");
      await expect(viestiField.errorFeedback).not.toBeVisible();
      await expect(kirjoitaViestiForm.lahetaButton).toBeDisabled();
    });

    await test.step("has required fields", async () => {
      await aiheField.input.fill("Aihe");
      await expect(kirjoitaViestiForm.lahetaButton).toBeDisabled();
      await viestiField.input.fill("Viesti");
      await expect(kirjoitaViestiForm.lahetaButton).toBeEnabled();
      await aiheField.input.fill("");
      await expect(kirjoitaViestiForm.lahetaButton).toBeDisabled();
    });

    await test.step("fields validate input", async () => {
      await aiheField.input.fill("");
      await expect(aiheField.errorFeedback).toBeVisible();
      await expect(aiheField.errorFeedback).toHaveText("Aihe on pakollinen");

      const aiheFieldMaxLength = 255;
      await aiheField.input.fill(stringOfLength(aiheFieldMaxLength));
      await expect(aiheField.errorFeedback).not.toBeVisible();
      await aiheField.input.fill(stringOfLength(aiheFieldMaxLength + 1));
      await expect(aiheField.errorFeedback).toHaveText(
        `Aihe on liian pitkä (${aiheFieldMaxLength + 1} merkkiä)`
      );

      await viestiField.input.fill("");
      await expect(viestiField.errorFeedback).toBeVisible();
      await expect(viestiField.errorFeedback).toHaveText(
        "Viesti on pakollinen"
      );
    });
  });

  test.skip("Viesti body has maximum length", async ({ page }) => {
    const osoitepalveluPage = new OsoitepalveluPage(page);
    const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
    const viestiField = kirjoitaViestiForm.viestiField;

    const viestiFieldMaxLength = 6291456;
    await viestiField.input.fill(stringOfLength(viestiFieldMaxLength));
    await expect(viestiField.errorFeedback).not.toBeVisible();
    await viestiField.input.fill(stringOfLength(viestiFieldMaxLength + 1));
    await expect(viestiField.errorFeedback).toHaveText(
      `Viesti on liian pitkä (${viestiFieldMaxLength + 1} merkkiä)`
    );
  });

  test.describe("Sending email to organisations", async () => {
    test.beforeEach(async ({ page }, testInfo) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await osoitepalveluPage.haeButton.click();
      await osoitepalveluPage.kirjoitaSahkopostiButton.click();
    });
    test("succesfully sending message shows 'Lähetys onnistui!' page", async ({
      page,
    }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
      await expect(kirjoitaViestiForm.lahetaButton).toBeDisabled();
      await kirjoitaViestiForm.aiheField.input.fill("Aihe");
      await kirjoitaViestiForm.viestiField.input.fill("Viesti");
      await expect(kirjoitaViestiForm.lahetaButton).toBeEnabled();
      await kirjoitaViestiForm.lahetaButton.click();
      await expect(page.getByText("Lähetys onnistui!")).toBeVisible();
    });
    test("failing to send message shows 'Lähetyksessä on viivettä' page", async ({
      request,
      page,
    }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
      await expect(kirjoitaViestiForm.lahetaButton).toBeDisabled();
      await kirjoitaViestiForm.aiheField.input.fill("Aihe");
      await kirjoitaViestiForm.viestiField.input.fill("Viesti");
      await expect(kirjoitaViestiForm.lahetaButton).toBeEnabled();
      await kirjoitaViestiForm.lahetaButton.click();
      await disableViestinvalitysIntegration(request);
      await expect(page.getByText("Lähetyksessä on viivettä")).toBeVisible();
      await enableViestinvalitysIntegration(request);
    });
    test("allows adding and removing attachments", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
      await uploadFile(page, kirjoitaViestiForm.fileUploadButton, "dummy.pdf");
      await expect(page.getByLabel("Poista liite dummy.pdf")).toBeVisible();
      await uploadFile(page, kirjoitaViestiForm.fileUploadButton, "dummy2.pdf");
      await expect(page.getByLabel("Poista liite dummy2.pdf")).toBeVisible();
      await page.getByLabel("Poista liite dummy.pdf").click();
      await expect(page.getByLabel("Poista liite dummy.pdf")).not.toBeVisible();
      await expect(page.getByLabel("Poista liite dummy2.pdf")).toBeVisible();
      await page.getByLabel("Poista liite dummy2.pdf").click();
      await expect(page.getByLabel("Poista liite dummy.pdf")).not.toBeVisible();
      await expect(
        page.getByLabel("Poista liite dummy2.pdf")
      ).not.toBeVisible();
    });

    test("prevents adding too large files", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
      await uploadFile(
        page,
        kirjoitaViestiForm.fileUploadButton,
        "nonsenselarge.file"
      );
      await expect(
        page.getByText("Liitetiedoston suurin sallittu koko on 4,5MB.")
      ).toBeVisible();
      await expect(page.getByLabel("Poista liite dummy.pdf")).not.toBeVisible();
    });
  });

  test.describe("Sending email to users", async () => {
    test.beforeEach(async ({ page }, testInfo) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      await osoitepalveluPage.koulutusotimijatKohderyhma.toggle();
      await osoitepalveluPage.palveluidenKayttajatKohderyhma.toggle();
      await osoitepalveluPage.kayttajaFilter.open();
      await selectFromKoulutustoimijaDropdown(
        page,
        "Testi Koulutuskuntayhtymä Puolukka"
      );
      await osoitepalveluPage.haeButton.click();
      await osoitepalveluPage.kirjoitaSahkopostiButton.click();
    });
    test("sending message shows 'Lähetys onnistui!' page", async ({ page }) => {
      const osoitepalveluPage = new OsoitepalveluPage(page);
      const kirjoitaViestiForm = osoitepalveluPage.kirjoitaViestiForm;
      await expect(kirjoitaViestiForm.lahetaButton).toBeDisabled();
      await kirjoitaViestiForm.aiheField.input.fill("Aihe");
      await kirjoitaViestiForm.viestiField.input.fill("Viesti");
      await expect(kirjoitaViestiForm.lahetaButton).toBeEnabled();
      await kirjoitaViestiForm.lahetaButton.click();
      await expect(page.getByText("Lähetys onnistui!")).toBeVisible();
    });
  });
});

test.describe("Osoitepalvelu generic error page", () => {
  test("shows generic error page when required request to backend fails", async ({
    page,
  }) => {
    await blockRequestOnce(page, "**/osoitteet/parametrit");
    const osoitepalveluPage = new OsoitepalveluPage(page);
    await osoitepalveluPage.goto();
    await expect(page.getByText("Osoitepalvelu ei vastaa")).toBeVisible();
    await page.getByText("Yritä uudelleen").click();
    await expect(page.getByText("Haun rajausmahdollisuudet")).toBeVisible();
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

async function selectFromKoulutustoimijaDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!(await page.isVisible(".koulutustoimija-react-select__menu"))) {
    await openDropdown(page, "Hae käyttäjiä koulutustoimijan nimellä");
  }
  await page.getByLabel(label, { exact: true }).click();
}

async function selectFromKayttooikeusryhmaDropdown(page: Page, label: string) {
  // This is required on webkit tests as the dropdown closes after selection
  if (!(await page.isVisible(".kayttooikeusryhma-react-select__menu"))) {
    await openDropdown(page, "Hae käyttäjiä käyttöoikeusryhmillä");
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

function stringOfLength(n: number) {
  return "x".repeat(n);
}

async function blockRequestOnce(page: Page, url: string) {
  let requestBlockedOnce = false;
  await page.route(url, (route) => {
    if (!requestBlockedOnce) {
      route.abort();
      requestBlockedOnce = true;
    } else {
      route.continue();
    }
  });
}

async function uploadFile(page: Page, locator: Locator, fileName: string) {
  const fileChooserPromise = page.waitForEvent("filechooser");
  await locator.click();
  const fileChooser = await fileChooserPromise;
  await fileChooser.setFiles(path.join(__dirname, "..", "resources", fileName));
}

async function enableViestinvalitysIntegration(request: APIRequestContext) {
  return request.post(
    "http://localhost:3003/organisaatio-service/mock/viestinvalitys/enableIntegration"
  );
}

async function disableViestinvalitysIntegration(request: APIRequestContext) {
  return request.post(
    "http://localhost:3003/organisaatio-service/mock/viestinvalitys/disableIntegration"
  );
}
