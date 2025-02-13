import { Locator, Page } from "@playwright/test";
import { selectAll } from "../LexicalUtil";
import { FormView } from "../FormView";

export class LomakeView extends FormView {
  readonly page: Page;
  readonly nimi: Locator;
  readonly muokkaaNimeaButton: Locator;
  readonly muokkaaNimea: MuokkaaNimeaModal;
  readonly nimihistoriaAccordion: Locator;
  readonly nimihistoriaPanel: Locator;
  readonly koskiPostiAccordion: Locator;
  readonly koskiPostiPanel: Locator;
  readonly rakenneAccordion: Locator;
  readonly rakennePanel: Locator;
  readonly poistaAjastettuNimenmuutos: Locator;
  readonly tallennaButton: Locator;

  constructor(page: Page) {
    super(page);
    this.page = page;
    this.nimi = page.getByTestId("organisation-name");
    this.muokkaaNimeaButton = page.getByText(
      "PERUSTIETO_MUOKKAA_ORGANISAATION_NIMEA_FI"
    );
    this.muokkaaNimea = new MuokkaaNimeaModal(page);
    this.nimihistoriaAccordion = page.locator(
      "#accordion__heading-nimihistorialomake"
    );
    this.nimihistoriaPanel = page.locator(
      "#accordion__panel-nimihistorialomake"
    );
    this.koskiPostiAccordion = page.locator(
      "#accordion__heading-koskipostilomake"
    );
    this.koskiPostiPanel = page.locator("#accordion__panel-koskipostilomake");
    this.rakenneAccordion = page.locator(
      "#accordion__heading-organisaatiohistorialomake"
    );
    this.rakennePanel = page.locator(
      "#accordion__panel-organisaatiohistorialomake"
    );
    this.poistaAjastettuNimenmuutos = page.getByTitle(
      "POISTA_AJASTETTU_NIMENMUUTOS"
    );
    this.tallennaButton = page.getByText("BUTTON_TALLENNA_FI");
  }

  async goto(oid: string) {
    await this.page.goto(
      `http://localhost:3003/organisaatio-service/lomake/${oid}`
    );
  }

  async gotoUusi(parentOid?: string) {
    await this.page.goto(
      `http://localhost:3003/organisaatio-service/lomake/uusi${
        parentOid ? `?parentOid=${parentOid}` : ""
      }`
    );
  }

  async fillYhteystiedot() {
    await this.page.locator("#accordion__heading-yhteystietolomake").click();
    await this.fillInput("sv.postiOsoite", "osoite");
    await this.fillInput("sv.postiOsoitePostiNro", "00100");
    await this.fillInput("sv.email", "email@test.fi");
  }
}

class MuokkaaNimeaModal {
  readonly page: Page;
  readonly createRadioButton: Locator;
  readonly editRadioButton: Locator;
  readonly copyNameButton: Locator;
  readonly vahvistaButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createRadioButton = page
      .getByTestId("CREATE_NAME_CHANGE")
      .locator("..");
    this.editRadioButton = page.getByTestId("EDIT_NAME_CHANGE").locator("..");
    this.copyNameButton = page.getByTitle("KOPIOI_MUIHIN_NIMIIN");
    this.vahvistaButton = page.getByText("BUTTON_VAHVISTA_FI");
  }

  async fillInput(name: string, text: string) {
    await this.page.locator(`input[name="${name}"]`).focus();
    await selectAll(this.page);
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(text);
  }

  async setDate(date: string) {
    await this.page
      .getByText("LABEL_NIMI_ALKUPVM")
      .locator("..")
      .locator("input")
      .focus();
    await selectAll(this.page);
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(date);
  }
}
