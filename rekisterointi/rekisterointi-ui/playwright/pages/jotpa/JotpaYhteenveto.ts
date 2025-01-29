import { Page } from '@playwright/test';

export function JotpaYhteenvetoFn(page: Page) {
    const locators = {
        yrityksenNimi: page.locator('[data-test-id="yrityksen-nimi"]'),
        ytunnus: page.locator('[data-test-id="ytunnus"]'),
        yritysmuoto: page.locator('[data-test-id="yritysmuoto"]'),
        organisaatiotyyppi: page.locator('[data-test-id="organisaatiotyyppi"]'),
        kotipaikka: page.locator('[data-test-id="kotipaikka"]'),
        alkamisaika: page.locator('[data-test-id="alkamisaika"]'),
        puhelinnumero: page.locator('[data-test-id="puhelinnumero"]'),
        organisaatioEmail: page.locator('[data-test-id="organisaatio-email"]'),
        postiosoite: page.locator('[data-test-id="postiosoite"]'),
        postinumero: page.locator('[data-test-id="postinumero"]'),
        postitoimipaikka: page.locator('[data-test-id="postitoimipaikka"]'),
        kayntiosoite: page.locator('[data-test-id="kayntiosoite"]'),
        kayntipostinumero: page.locator('[data-test-id="kayntipostinumero"]'),
        kayntipostitoimipaikka: page.locator('[data-test-id="kayntipostitoimipaikka"]'),
        emails: page.locator('[data-test-id="emails"]'),
        etunimi: page.locator('[data-test-id="etunimi"]'),
        sukunimi: page.locator('[data-test-id="sukunimi"]'),
        paakayttajaEmail: page.locator('[data-test-id="paakayttaja-email"]'),
        asiointikieli: page.locator('[data-test-id="asiointikieli"]'),
        saateteksti: page.locator('[data-test-id="info"]'),
    };

    async function submit() {
        await page.locator('input[type="submit"]').click();
        await page.waitForNavigation();
    }

    return {
        locators,
        submit,
    };
}
