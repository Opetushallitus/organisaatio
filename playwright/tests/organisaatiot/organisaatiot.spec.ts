import { expect, Page, test } from "@playwright/test";

import { persistOrganisation } from "../organisations";
import { LomakeView } from "./LomakeView";
import { NewApiOrganisaatio } from "../../../organisaatio-ui/src/types/apiTypes";

const createAndGotoLomake = async (
  page: Page,
  prefix: string,
  override: Partial<NewApiOrganisaatio>
) => {
  const response = await persistOrganisation(prefix, override);

  const organisaatioPage = new LomakeView(page);
  await organisaatioPage.goto(response.organisaatio.oid);
  await expect(organisaatioPage.nimi).toContainText(prefix + " Suominimi");

  return organisaatioPage;
};

test.describe("Organisations", () => {
  test.describe("Name Change", () => {
    test("Saves a new name", async ({ page }) => {
      const organisaatioPage = await createAndGotoLomake(page, "PARENT1", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      await organisaatioPage.muokkaaNimeaButton.click();
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.fi",
        "pöllö Suominimi"
      );
      await organisaatioPage.muokkaaNimea.fillInput("nimi.sv", "pöllö Ruotsi");
      await organisaatioPage.muokkaaNimea.fillInput("nimi.en", "pöllö Enkku");
      await organisaatioPage.muokkaaNimea.vahvistaButton.click();

      await expect(organisaatioPage.nimi).toContainText("pöllö Suominimi");
    });

    test("Edits a name", async ({ page }) => {
      const organisaatioPage = await createAndGotoLomake(page, "PARENT2", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      await organisaatioPage.muokkaaNimeaButton.click();
      await organisaatioPage.muokkaaNimea.editRadioButton.click();
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.fi",
        "pöllö Suominimi muokattu"
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.sv",
        "pöllö Ruotsi muokattu"
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.en",
        "pöllö Enkku muokattu"
      );
      await organisaatioPage.muokkaaNimea.vahvistaButton.click();

      await expect(organisaatioPage.nimi).toContainText(
        "pöllö Suominimi muokattu"
      );
    });

    test("Copies edited name to other fields", async ({ page }) => {
      const organisaatioPage = await createAndGotoLomake(page, "PARENT3", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      await organisaatioPage.muokkaaNimeaButton.click();
      await organisaatioPage.muokkaaNimea.editRadioButton.click();
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.fi",
        "pöllö testi kopioitava"
      );
      await organisaatioPage.muokkaaNimea.copyNameButton.click();
      await organisaatioPage.muokkaaNimea.vahvistaButton.click();

      await expect(
        page.getByText(
          "pöllö testi kopioitava [fi], pöllö testi kopioitava [sv], pöllö testi kopioitava [en]"
        )
      ).toBeVisible();
    });

    test("Schedules a name change", async ({ page }) => {
      const organisaatioPage = await createAndGotoLomake(page, "PARENT4", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      await organisaatioPage.muokkaaNimeaButton.click();
      const date = new Date();
      await organisaatioPage.muokkaaNimea.setDate(
        `2.${date.getMonth() + 1}.${date.getFullYear() + 1}`
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.fi",
        "pöllö ajastettu Suominimi"
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.sv",
        "pöllö ajastettu Ruotsi"
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.en",
        "pöllö ajastettu Enkku"
      );
      await organisaatioPage.muokkaaNimea.vahvistaButton.click();

      await expect(organisaatioPage.nimi).toContainText("PARENT4 Suominimi");
      await organisaatioPage.nimihistoriaAccordion.click();
      await expect(organisaatioPage.nimihistoriaPanel).toContainText(
        "pöllö ajastettu Suominimi [fi], pöllö ajastettu Ruotsi [sv], pöllö ajastettu Enkku [en]"
      );
      await expect(organisaatioPage.poistaAjastettuNimenmuutos).toBeVisible();
    });

    test("Deletes a name change", async ({ page }) => {
      const organisaatioPage = await createAndGotoLomake(page, "PARENT4", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      await organisaatioPage.muokkaaNimeaButton.click();
      const date = new Date();
      await organisaatioPage.muokkaaNimea.setDate(
        `2.${date.getMonth() + 1}.${date.getFullYear() + 1}`
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.fi",
        "pöllö delete Suominimi"
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.sv",
        "pöllö delete Ruotsi"
      );
      await organisaatioPage.muokkaaNimea.fillInput(
        "nimi.en",
        "pöllö delete Enkku"
      );
      await organisaatioPage.muokkaaNimea.vahvistaButton.click();

      await expect(organisaatioPage.nimi).toContainText("PARENT4 Suominimi");
      await organisaatioPage.nimihistoriaAccordion.click();
      await expect(organisaatioPage.nimihistoriaPanel).toContainText(
        "pöllö delete Suominimi [fi], pöllö delete Ruotsi [sv], pöllö delete Enkku [en]"
      );
      page.on("dialog", (dialog) => dialog.accept());
      await organisaatioPage.poistaAjastettuNimenmuutos.click();
      await expect(organisaatioPage.nimihistoriaPanel).not.toContainText(
        "pöllö delete Suominimi [fi], pöllö delete Ruotsi [sv], pöllö delete Enkku [en]"
      );
    });
  });
});
