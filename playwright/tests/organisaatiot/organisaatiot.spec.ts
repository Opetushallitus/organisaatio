import { expect, Page, test } from "@playwright/test";
import { FinnishBusinessIds } from "finnish-business-ids";
import moment from "moment";

import {
  helsinki,
  organisaatio,
  persistOrganisation,
  persistOrganisationWithPrefix,
} from "../organisations";
import { LomakeView } from "./LomakeView";
import { NewApiOrganisaatio } from "../../../organisaatio-ui/src/types/apiTypes";
import { KOSKIPOSTI_BASE } from "../../../organisaatio-ui/src/contexts/constants";
import { RyhmatView, RyhmaEditView } from "./RyhmatView";
import { ryhmat } from "./ryhmat";
import { OrganisaatiotView } from "./OrganisaatiotView";
import { ytjHameen } from "./ytjHameen";
import { before } from "node:test";

const createAndGotoLomake = async (
  page: Page,
  prefix: string,
  override: Partial<NewApiOrganisaatio>
) => {
  const response = await persistOrganisationWithPrefix(prefix, override);

  const organisaatioPage = new LomakeView(page);
  await organisaatioPage.goto(response.organisaatio.oid);
  await expect(organisaatioPage.nimi).toContainText(prefix + " Suominimi");

  return organisaatioPage;
};

const baseCasMe = {
  uid: "mbender",
  oid: "1.2.246.562.24.41721051355",
  firstName: "Mike",
  lastName: "Bender",
  groups: ["LANG_fi", "VIRKAILIJA"],
  roles:
    '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD","LANG_fi","USER_mbender","VIRKAILIJA"]',
  lang: "fi",
};
const taulukkoLisaaUusi = "TAULUKKO_LISAA_UUSI";
const poistaOrganisaatio = "LOMAKE_POISTA_ORGANISAATIO";
const lomakeLisaaUusi = "LISAA_UUSI";
const tallenna = "TALLENNA";
const oppilaitosButtons = ["SIIRRA", "YHDISTA"];
const koulutusToimijaButtons = ["PERUSTIETO_PAIVITA_YTJ_TIEDOT"];
const generalRestrictedButtons = [
  "MUOKKAA_ORGANISAATION_NIMEA",
  "PERUSTIETO_MERKITSE_ORGANISAATIO_LAKKAUTETUKSI",
];
const suljeButton = "SULJE";
const allRestrictedButtons = [
  lomakeLisaaUusi,
  ...oppilaitosButtons,
  ...koulutusToimijaButtons,
  ...generalRestrictedButtons,
];

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

  test.describe("Organisaatiot View", () => {
    test.describe("Restricts buttons by roles", () => {
      let parent: string;

      test.beforeAll(async () => {
        const p = await persistOrganisationWithPrefix("PARENT1", {
          tyypit: [`organisaatiotyyppi_01`],
        });
        parent = p.organisaatio.oid;
      });

      test("does not show buttons without required roles", async ({ page }) => {
        await page.route(`**/cas/me`, (route) =>
          route.fulfill({
            status: 200,
            body: JSON.stringify({
              ...baseCasMe,
              roles:
                '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD"]',
            }),
          })
        );

        const organisaatiotPage = new OrganisaatiotView(page);
        await organisaatiotPage.goto();
        await expect(page.getByText(taulukkoLisaaUusi)).not.toBeVisible();
      });

      test("does not show buttons with CRUD roles", async ({ page }) => {
        await page.route(`**/cas/me`, (route) =>
          route.fulfill({
            status: 200,
            body: JSON.stringify({
              ...baseCasMe,
              roles: `["APP_ORGANISAATIOHALLINTA_CRUD","APP_ORGANISAATIOHALLINTA_CRUD_${parent}"]`,
            }),
          })
        );

        const organisaatiotPage = new OrganisaatiotView(page);
        await organisaatiotPage.goto();
        await expect(page.getByText(taulukkoLisaaUusi)).not.toBeVisible();
      });

      test("shows buttons with OPH roles", async ({ page }) => {
        await page.route(`**/cas/me`, (route) =>
          route.fulfill({
            status: 200,
            body: JSON.stringify({
              uid: "mbender",
              oid: "1.2.246.562.24.41721051355",
              firstName: "Mike",
              lastName: "Bender",
              groups: ["LANG_fi", "VIRKAILIJA"],
              roles:
                '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD","APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001","LANG_fi","USER_mbender","VIRKAILIJA"]',
              lang: "fi",
            }),
          })
        );

        const organisaatiotPage = new OrganisaatiotView(page);
        await organisaatiotPage.goto();
        await expect(page.getByText(taulukkoLisaaUusi)).toBeVisible();
      });
    });

    test("can filter by name", async ({ page }) => {
      const organisaatiotPage = new OrganisaatiotView(page);
      await organisaatiotPage.goto();

      organisaatiotPage.filterByName("Mustikkalan testi op");

      await expect(
        organisaatiotPage.organisaatioLink("Mustikkalan testi opisto")
      ).toBeVisible();
    });

    test("Organisaatiotarkastus", async ({ page }) => {
      const now = moment().format("yyyyMMDDssS");
      const today = moment();
      const timesPast = moment().subtract(2, "years");
      const recent = moment().subtract(23, "days");
      const prefix = `${now} TARKASTUS IS `;
      const ui_date_format = "D.M.yyyy";

      const org1 = await persistOrganisationWithPrefix(`${prefix}NOT CHECKED`, {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const org2 = await persistOrganisationWithPrefix(
        `${prefix}CHECKED LONG AGO`,
        {
          tyypit: [`organisaatiotyyppi_01`],
          tarkastusPvm: timesPast.unix() * 1000,
        }
      );
      const org3 = await persistOrganisationWithPrefix(
        `${prefix}CHECKED LATELY`,
        {
          tyypit: [`organisaatiotyyppi_01`],
          tarkastusPvm: recent.unix() * 1000,
        }
      );
      const organisaatiotPage = new OrganisaatiotView(page);
      await organisaatiotPage.goto();
      await organisaatiotPage.filterByName(prefix);

      await test.step("shows flags when tarkastus missing", async () => {
        await expect(
          page
            .getByRole("row", { name: `${now} TARKASTUS IS NOT` })
            .getByTitle("TARKASTUS_PUUTTUU")
        ).toBeVisible();
      });

      await test.step("shows flags when tarkastus done", async () => {
        await expect(
          page
            .getByRole("row", { name: `${now} TARKASTUS IS CHECKED LATELY` })
            .getByTitle(`VIIMEINEN_TARKASTUS_${recent.format(ui_date_format)}`)
        ).toBeVisible();
      });

      await test.step("shows flags when tarkastus old", async () => {
        await expect(
          page
            .getByRole("row", { name: `${now} TARKASTUS IS CHECKED LONG AGO` })
            .getByTitle(
              `VIIMEINEN_TARKASTUS_${timesPast.format(ui_date_format)}`
            )
        ).toBeVisible();
      });

      await test.step("sets tarkastus date", async () => {
        await organisaatiotPage
          .organisaatioLink("TARKASTUS IS CHECKED LONG AGO")
          .click();
        await page
          .getByTitle(`VIIMEINEN_TARKASTUS_${timesPast.format(ui_date_format)}`)
          .click();
        await expect(
          page.getByTitle(`VIIMEINEN_TARKASTUS_${today.format(ui_date_format)}`)
        ).toBeVisible();
      });

      await test.step("shows new tarkastus date", async () => {
        await organisaatiotPage.goto();
        await organisaatiotPage.filterByName(prefix);
        await expect(
          page
            .getByRole("row", { name: `${now} TARKASTUS IS CHECKED LONG AGO` })
            .getByTitle(`VIIMEINEN_TARKASTUS_${today.format(ui_date_format)}`)
        ).toBeVisible();
      });
    });
  });

  test.describe("Organisaatio Lomake View", () => {
    test.describe("Restricts buttons by roles", () => {
      let parent: string, child: string, grandchild: string;

      test.beforeAll(async () => {
        const p = await persistOrganisationWithPrefix("PARENT1", {
          tyypit: [`organisaatiotyyppi_01`],
        });
        parent = p.organisaatio.oid;
        const c = await persistOrganisationWithPrefix("CHILD", {
          parentOid: p.organisaatio.oid,
          tyypit: [`organisaatiotyyppi_02`],
        });
        child = c.organisaatio.oid;
        const g = await persistOrganisationWithPrefix("GRANDCHILD", {
          parentOid: c.organisaatio.oid,
          tyypit: [`organisaatiotyyppi_03`],
        });
        grandchild = g.organisaatio.oid;
      });

      test("does not show buttons without required roles", async ({ page }) => {
        await page.route(`**/cas/me`, (route) =>
          route.fulfill({
            status: 200,
            body: JSON.stringify({
              ...baseCasMe,
              roles:
                '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD"]',
            }),
          })
        );

        const organisaatioPage = new LomakeView(page);

        await test.step("test for Koulutustoimija", async () => {
          await organisaatioPage.goto(parent);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of allRestrictedButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
        });

        await test.step("test for Oppilaitos", async () => {
          await organisaatioPage.goto(child);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of allRestrictedButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
        });

        await test.step("test for Toimipiste", async () => {
          await organisaatioPage.goto(grandchild);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of allRestrictedButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
        });
      });

      test("shows buttons with CRUD roles", async ({ page }) => {
        await page.route(`**/cas/me`, (route) =>
          route.fulfill({
            status: 200,
            body: JSON.stringify({
              ...baseCasMe,
              roles: `["APP_ORGANISAATIOHALLINTA_CRUD","APP_ORGANISAATIOHALLINTA_CRUD_${parent}"]`,
            }),
          })
        );

        const organisaatioPage = new LomakeView(page);

        await test.step("test for Koulutustoimija", async () => {
          await organisaatioPage.goto(parent);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of allRestrictedButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
        });

        await test.step("test for Oppilaitos", async () => {
          await organisaatioPage.goto(child);
          await expect(page.getByText(suljeButton)).toBeVisible();
          const visibleButtons = [lomakeLisaaUusi, suljeButton, tallenna];
          const restrictedButtons = [
            ...oppilaitosButtons,
            ...koulutusToimijaButtons,
            ...generalRestrictedButtons,
            poistaOrganisaatio,
          ];
          for (const button of visibleButtons) {
            await expect(page.getByText(button)).toBeVisible();
          }
          for (const button of restrictedButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
        });

        await test.step("test for Toimipiste", async () => {
          await organisaatioPage.goto(grandchild);
          await expect(page.getByText(suljeButton)).toBeVisible();
          const visibleButtons = [
            lomakeLisaaUusi,
            ...generalRestrictedButtons,
            suljeButton,
            tallenna,
          ];
          const restrictedButtons = [
            ...oppilaitosButtons,
            ...koulutusToimijaButtons,
            poistaOrganisaatio,
          ];
          for (const button of visibleButtons) {
            await expect(page.getByText(button)).toBeVisible();
          }
          for (const button of restrictedButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
        });
      });

      test("shows buttons for OPH roles", async ({ page }) => {
        await page.route(`**/cas/me`, (route) =>
          route.fulfill({
            status: 200,
            body: JSON.stringify({
              uid: "mbender",
              oid: "1.2.246.562.24.41721051355",
              firstName: "Mike",
              lastName: "Bender",
              groups: ["LANG_fi", "VIRKAILIJA"],
              roles:
                '["APP_ORGANISAATIOHALLINTA","APP_ORGANISAATIOHALLINTA_CRUD","APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001","LANG_fi","USER_mbender","VIRKAILIJA"]',
              lang: "fi",
            }),
          })
        );

        const organisaatioPage = new LomakeView(page);

        await test.step("test for Koulutustoimija", async () => {
          await organisaatioPage.goto(parent);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of oppilaitosButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
          const visibleButtons = [
            ...koulutusToimijaButtons,
            lomakeLisaaUusi,
            suljeButton,
            ...generalRestrictedButtons,
            tallenna,
            poistaOrganisaatio,
          ];
          for (const button of visibleButtons) {
            await expect(page.getByText(button)).toBeVisible();
          }
        });

        await test.step("test for Oppilaitos", async () => {
          await organisaatioPage.goto(child);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of koulutusToimijaButtons) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
          const visibleButtons = [
            ...generalRestrictedButtons,
            ...oppilaitosButtons,
            lomakeLisaaUusi,
            suljeButton,
            tallenna,
            poistaOrganisaatio,
          ];
          for (const button of visibleButtons) {
            await expect(page.getByText(button)).toBeVisible();
          }
        });

        await test.step("test for Toimipiste", async () => {
          await organisaatioPage.goto(grandchild);
          await expect(page.getByText(suljeButton)).toBeVisible();
          for (const button of [
            ...oppilaitosButtons,
            ...koulutusToimijaButtons,
          ]) {
            await expect(page.getByText(button)).not.toBeVisible();
          }
          const visibleButtons = [
            lomakeLisaaUusi,
            ...generalRestrictedButtons,
            suljeButton,
            tallenna,
            poistaOrganisaatio,
          ];
          for (const button of visibleButtons) {
            await expect(page.getByText(button)).toBeVisible();
          }
        });
      });
    });

    test("shows oppilaitos specific fields", async ({ page }) => {
      const organisaatioPage = new LomakeView(page);
      const response = await persistOrganisationWithPrefix("PARENT1", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      await organisaatioPage.gotoUusi(response.organisaatio.oid);

      await page.getByText("Oppilaitos").click();
      await expect(page.getByText("PERUSTIETO_OPPILAITOSKOODI")).toBeVisible();
      await expect(page.getByText("PERUSTIETO_OPPILAITOSTYYPPI")).toBeVisible();
      await expect(
        page.getByText("PERUSTIETO_MUUT_OPPILAITOSTYYPPI")
      ).toBeVisible();
      await expect(
        page.getByText("PERUSTIETO_OPPILAITOS_MUUTOKSET")
      ).toBeVisible();

      await expect(page.getByText("VUOSILUOKAT")).not.toBeVisible();
      await organisaatioPage.selectFromDropdown(
        "oppilaitosTyyppiUri",
        "Peruskoulut"
      );
      await expect(page.getByText("VUOSILUOKAT")).toBeVisible();
      await organisaatioPage.selectFromDropdown(
        "oppilaitosTyyppiUri",
        "Lukiot"
      );
      await expect(page.getByText("VUOSILUOKAT")).not.toBeVisible();
      await organisaatioPage.selectFromDropdown(
        "oppilaitosTyyppiUri",
        "Perus- ja lukioasteen koulut"
      );
      await expect(page.getByText("VUOSILUOKAT")).toBeVisible();
      await organisaatioPage.selectFromDropdown(
        "oppilaitosTyyppiUri",
        "Peruskouluasteen erityiskoulut"
      );
      await expect(page.getByText("VUOSILUOKAT")).toBeVisible();
    });

    test("shows and edits koskiposti fields", async ({ page }) => {
      const parentResponse = await persistOrganisationWithPrefix("PARENT1", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const childResponse = await persistOrganisationWithPrefix("CHILD", {
        parentOid: parentResponse.organisaatio.oid,
        tyypit: [`organisaatiotyyppi_02`],
        yhteystietoArvos: [
          {
            ...KOSKIPOSTI_BASE,
            "YhteystietoArvo.arvoText": "testi@testi.com",
            "YhteystietoArvo.kieli": "kieli_fi#1",
          },
        ],
      });

      const organisaatioPage = new LomakeView(page);
      organisaatioPage.goto(childResponse.organisaatio.oid);
      await page
        .locator("#accordion__panel-perustietolomake")
        .getByText("Oppilaitos", { exact: true })
        .click();
      await expect(page.getByText("LOMAKE_KOSKI_POSTI")).not.toBeVisible();
      await page
        .locator("#accordion__panel-perustietolomake")
        .getByText("Oppilaitos", { exact: true })
        .click();
      await expect(page.getByText("LOMAKE_KOSKI_POSTI")).toBeVisible();
      await organisaatioPage.koskiPostiAccordion.click();
      await organisaatioPage.fillInput("koskiposti.fi", "muokattufi@testi.com");
      await organisaatioPage.fillInput("koskiposti.sv", "muokattusv@testi.com");
      await organisaatioPage.fillInput("koskiposti.en", "muokattuen@testi.com");
      await organisaatioPage.fillYhteystiedot("sv");
      await organisaatioPage.tallennaButton.click();
      await expect(
        page.getByText("MESSAGE_TALLENNUS_ONNISTUI_FI")
      ).toBeVisible();

      organisaatioPage.goto(childResponse.organisaatio.oid);
      await organisaatioPage.koskiPostiAccordion.click();
      await expect(page.locator('input[name="koskiposti.fi"]')).toHaveValue(
        "muokattufi@testi.com"
      );
      await expect(page.locator('input[name="koskiposti.sv"]')).toHaveValue(
        "muokattusv@testi.com"
      );
      await expect(page.locator('input[name="koskiposti.en"]')).toHaveValue(
        "muokattuen@testi.com"
      );
    });

    test("shows VAKA specific fields", async ({ page }) => {
      const parent = await persistOrganisationWithPrefix("PARENT1", {
        tyypit: [`organisaatiotyyppi_07`],
      });
      const child = await persistOrganisationWithPrefix("CHILD", {
        parentOid: parent.organisaatio.oid,
        tyypit: [`organisaatiotyyppi_08`],
        piilotettu: true,
        varhaiskasvatuksenToimipaikkaTiedot: {
          toimintamuoto: "vardatoimintamuoto_tm02",
          kasvatusopillinenJarjestelma:
            "vardakasvatusopillinenjarjestelma_kj98",
          varhaiskasvatuksenJarjestamismuodot: [
            "vardajarjestamismuoto_jm01",
            "vardajarjestamismuoto_jm02",
          ],
          paikkojenLukumaara: 4,
          varhaiskasvatuksenKielipainotukset: [
            {
              kielipainotus: "kieli_fi",
              alkupvm: "2021-11-01",
            },
            {
              kielipainotus: "kieli_99",
              alkupvm: "2005-11-01",
            },
          ],
          varhaiskasvatuksenToiminnallinenpainotukset: [
            {
              toiminnallinenpainotus: "vardatoiminnallinenpainotus_tp06",
              alkupvm: "2021-11-01",
            },
            {
              toiminnallinenpainotus: "vardatoiminnallinenpainotus_tp07",
              alkupvm: "2020-11-01",
              loppupvm: "2021-05-05",
            },
          ],
        },
      });

      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.goto(child.organisaatio.oid);
      await expect(
        page.getByRole("heading", { name: "Varhaiskasvatuksen toimipaikka" })
      ).toBeVisible();
      await expect(
        page.locator('input[name="organisaatiotyyppi_08"]')
      ).toBeChecked();
      await expect(
        page.locator('input[name="organisaatiotyyppi_08"]')
      ).toBeDisabled();
      await organisaatioPage.vakaAccordion.click();
      await expect(
        organisaatioPage.vakaPanel.getByText("Perhepäivähoito")
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText("Ei painotusta")
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText("Ympäristö ja luonto")
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText("Seikkailu")
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText(
          "Kunnan tai kuntayhtymän järjestämä",
          { exact: true }
        )
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText(
          "Ostopalvelu, kunnan tai kuntayhtymän järjestämä"
        )
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText("tuntematon 1.11.2005")
      ).toBeVisible();
      await expect(
        organisaatioPage.vakaPanel.getByText("suomi 1.11.2021")
      ).toBeVisible();
    });

    test("marks organisation removed", async ({ page }) => {
      const response = await persistOrganisation(helsinki({}));
      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.goto(response.organisaatio.oid);

      await expect(
        page.getByText("Helsingin kaupunki", { exact: true })
      ).toBeVisible();
      await page.locator("button[name=LOMAKE_POISTA_ORGANISAATIO]").click();
      await page.locator("button[name=BUTTON_VAHVISTA]").click();

      await organisaatioPage.goto(response.organisaatio.oid);
      await expect(
        page.getByText("Helsingin kaupunki (LABEL_POISTETTU)", { exact: true })
      ).toBeVisible();
    });

    test("creates a new organisation with y-tunnus", async ({ page }) => {
      const yTunnus = FinnishBusinessIds.generateBusinessId();
      await page.route(`**/rest/ytj/${yTunnus}`, (route) =>
        route.fulfill({
          status: 200,
          body: JSON.stringify(ytjHameen(yTunnus)),
        })
      );

      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.gotoUusi("1.2.246.562.10.00000000001");

      await page.getByText("Koulutustoimija").click();
      await page.getByText("HAE_YTJ_TIEDOT").click();
      await organisaatioPage.fillInput("ytjinput", yTunnus);
      await page.getByText("HAE_YTJTIEDOT").click();
      await page.getByText("Hameen ammatti").click();
      await page.getByText("BUTTON_JATKA").click();
      await organisaatioPage.tallennaButton.click();

      await expect(
        page.getByText("MESSAGE_TALLENNUS_ONNISTUI_FI")
      ).toBeVisible();
      await expect(page.locator("h1")).toHaveText(
        " Hameen ammatti-instituutti Oy 4"
      );
    });

    test("edits existing organisation with info from ytj", async ({ page }) => {
      const yTunnus = FinnishBusinessIds.generateBusinessId();
      await page.route(`**/rest/ytj/${yTunnus}`, (route) =>
        route.fulfill({
          status: 200,
          body: JSON.stringify(ytjHameen(yTunnus)),
        })
      );

      const response = await persistOrganisation(organisaatio("BERFORE_FETCH"));
      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.goto(response.organisaatio.oid);
      await page.getByText("PAIVITA_YTJ_TIEDOT").click();
      await organisaatioPage.fillInput("ytjinput", yTunnus);
      await page.getByText("HAE_YTJTIEDOT").click();
      await page.getByText("Hameen ammatti").click();
      await organisaatioPage.tallennaButton.click();

      await expect(
        page.getByText("MESSAGE_TALLENNUS_ONNISTUI_FI")
      ).toBeVisible();
      await expect(page.locator("h1")).toHaveText(
        " Hameen ammatti-instituutti Oy 4"
      );
    });

    test("saves child organisation", async ({ page }) => {
      const response = await persistOrganisationWithPrefix("PARENT", {});
      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.goto(response.organisaatio.oid);

      await page.getByText("LISAA_UUSI_TOIMIJA").click();
      await page
        .locator("#accordion__panel-perustietolomake")
        .getByText("Oppilaitos", { exact: true })
        .click();
      await organisaatioPage.fillInput("nimi.fi", "CHILD Suominimi");
      await organisaatioPage.fillInput("nimi.sv", "CHILD Ruotsi");
      await organisaatioPage.fillInput("nimi.en", "CHILD Enkku");
      await organisaatioPage.selectFromDropdown(
        "PERUSTIETO_PAASIJAINTIKUNTA_SELECT",
        "Ranua"
      );
      await organisaatioPage.selectFromDropdown(
        "PERUSTIETO_MAA_SELECT",
        "Andorra"
      );
      await organisaatioPage.selectFromDropdown(
        "PERUSTIETO_OPETUSKIELI_SELECT",
        "ruotsi"
      );
      await organisaatioPage.setDate("PERUSTAMISPAIVA", "2.9.2021");

      await page.getByText("JATKA").click();
      await page.getByText("NAYTA_MUUT_KIELET").click();
      await organisaatioPage.fillYhteystiedot("sv", true);
      await organisaatioPage.fillYhteystiedot("fi", true);

      await organisaatioPage.tallennaButton.click();

      await expect(
        page.getByText("MESSAGE_TALLENNUS_ONNISTUI_FI")
      ).toBeVisible();
      await page.reload();
      await organisaatioPage.rakenneAccordion.click();
      await expect(
        organisaatioPage.rakennePanel.getByText("PARENT Suominimi")
      ).toBeVisible();
    });

    test("merges organisations", async ({ page }) => {
      const parent1 = await persistOrganisationWithPrefix("PARENT1", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const parent2 = await persistOrganisationWithPrefix("PARENT2", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const parent3 = await persistOrganisationWithPrefix("PARENT3", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      const child1 = await persistOrganisationWithPrefix("CHILD1", {
        parentOid: parent1.organisaatio.oid,
        tyypit: [`organisaatiotyyppi_02`],
      });
      const child2 = await persistOrganisationWithPrefix("CHILD2", {
        parentOid: parent2.organisaatio.oid,
        tyypit: [`organisaatiotyyppi_02`],
      });
      const child3 = await persistOrganisationWithPrefix("CHILD3", {
        parentOid: parent3.organisaatio.oid,
        tyypit: [`organisaatiotyyppi_02`],
      });

      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.goto(child2.organisaatio.oid);
      await page.getByText("LOMAKE_YHDISTA_ORGANISAATIO").click();
      await organisaatioPage.selectFromDropdown(
        "ORGANISAATIO_YHDISTYS_TOINEN_ORGANISAATIO",
        child3.organisaatio.ytunnus
      );
      await page.getByText("BUTTON_VAHVISTA").click();
      await expect(
        page.getByText("TOIMIPISTEEN_YHDISTYS_VAHVISTUS_TITLE")
      ).toBeVisible();
      await page.getByText("BUTTON_VAHVISTA").click();
      await organisaatioPage.rakenneAccordion.click();
      await expect(
        organisaatioPage.rakennePanel.getByText("CHILD3")
      ).toBeVisible();
      await expect(
        organisaatioPage.rakennePanel.getByText("PARENT2")
      ).toBeVisible();
    });

    test("moves organisations", async ({ page }) => {
      const parent1 = await persistOrganisationWithPrefix("PARENT1", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const parent2 = await persistOrganisationWithPrefix("PARENT2", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const parent3 = await persistOrganisationWithPrefix("PARENT3", {
        tyypit: [`organisaatiotyyppi_01`],
      });
      const parent4 = await persistOrganisationWithPrefix("PARENT4", {
        tyypit: [`organisaatiotyyppi_01`],
      });

      const child = await persistOrganisationWithPrefix("CHILD", {
        parentOid: parent1.organisaatio.oid,
        tyypit: [`organisaatiotyyppi_02`],
      });

      const organisaatioPage = new LomakeView(page);
      await organisaatioPage.goto(child.organisaatio.oid);
      await page.getByText("LOMAKE_SIIRRA_ORGANISAATIO").click();
      await organisaatioPage.selectFromDropdown(
        "ORGANISAATIO_SIIRTO_TOINEN_ORGANISAATIO",
        parent3.organisaatio.ytunnus
      );
      await page.getByText("BUTTON_VAHVISTA").click();
      await expect(
        page.getByText("Siirretäänkö CHILD Suominimi")
      ).toBeVisible();
      await page.getByText("BUTTON_VAHVISTA").click();
      await organisaatioPage.rakenneAccordion.click();
      await expect(
        organisaatioPage.rakennePanel.getByText("PARENT1 Suominimi")
      ).toBeVisible();
      await expect(
        organisaatioPage.rakennePanel.getByText("PARENT3 Suominimi")
      ).toBeVisible();
    });
  });

  test("Ryhmat View", async ({ page }) => {
    await page.route("**/ryhmat?aktiivinen=true", (route) =>
      route.fulfill({
        status: 200,
        body: JSON.stringify(ryhmat),
      })
    );

    const ryhmatPage = new RyhmatView(page);
    await ryhmatPage.goto();

    await test.step("can filter by ryhmätyyppi", async () => {
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).not.toBeVisible();
      await ryhmatPage.filterByRyhmatyyppi("Perustetyöryhmä");
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).toBeVisible();
      await ryhmatPage.clearRyhmatyyppi();
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).not.toBeVisible();
    });

    await test.step("can filter by käyttötarkoitus", async () => {
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).not.toBeVisible();
      await ryhmatPage.filterByKayttotarkoitus("Perusteiden laadinta");
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).toBeVisible();
      await ryhmatPage.clearKayttotarkoitus();
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).not.toBeVisible();
    });

    await test.step("can filter by tila", async () => {
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).not.toBeVisible();
      await ryhmatPage.filterByTila("Passiivinen");
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).toBeVisible();
      await ryhmatPage.clearTila();
      await expect(ryhmatPage.ryhmaLink("AM_opaslehtiset")).not.toBeVisible();
    });

    await test.step("can use table pagination", async () => {
      await expect(ryhmatPage.ryhmaLink("ADSDAS")).toBeVisible();
      await ryhmatPage.setPageNumber(2);
      await expect(ryhmatPage.ryhmaLink("ADSDAS")).not.toBeVisible();
      await expect(
        ryhmatPage.ryhmaLink(
          "AMK hakukohde kevät II 2020: EB, IB, RP ja DIA arvosanat"
        )
      ).toBeVisible();
      await ryhmatPage.setPageNumber(1);
      await expect(ryhmatPage.ryhmaLink("ADSDAS")).toBeVisible();
    });

    await test.step("can change amount shown on page", async () => {
      await expect(page.getByRole("table").locator("a")).toHaveCount(10);
      await ryhmatPage.setShownOnPage(20);
      await expect(page.getByRole("table").locator("a")).toHaveCount(20);
    });
  });

  test("Ryhma Edit View", async ({ page }) => {
    const ryhmatPage = new RyhmatView(page);
    const uusiRyhmaPage = new RyhmaEditView(page);
    const nimi = "Suominimi " + new Date();

    await test.step("Can save a new ryhma", async () => {
      await uusiRyhmaPage.gotoUusiRyhma();

      await uusiRyhmaPage.fillInput("nimiFi", nimi);
      await uusiRyhmaPage.fillInput("nimiSv", "Ruotsinimi");
      await uusiRyhmaPage.fillInput("nimiEn", "Enkkunimi");
      await uusiRyhmaPage.fillInput("kuvaus2Fi", "Suomi kuvaus");
      await uusiRyhmaPage.fillInput("kuvaus2Sv", "Ruotsi kuvaus");
      await uusiRyhmaPage.fillInput("kuvaus2En", "Enkku kuvaus");
      await uusiRyhmaPage.selectRyhmanTyyppi("Hakukohde");
      await uusiRyhmaPage.selectRyhmanKayttotarkoitus("Yleinen");
      await uusiRyhmaPage.tallennaButton.click();

      await ryhmatPage.filterByName(nimi);
      await expect(ryhmatPage.ryhmaLink(nimi)).toBeVisible();
    });

    await test.step("Can edit just saved Suominimi", async () => {
      const newNimi = "Parempi " + nimi;
      await ryhmatPage.ryhmaLink(nimi).click();
      const editRyhmaView = new RyhmaEditView(page);
      await editRyhmaView.fillInput("nimiFi", newNimi);
      await editRyhmaView.tallennaButton.click();

      await ryhmatPage.filterByName(newNimi);
      await expect(ryhmatPage.ryhmaLink(newNimi)).toBeVisible();
    });
  });
});
