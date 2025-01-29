import { Page } from '@playwright/test';

import { selectValue } from '../../util/react-select';

export function JotpaOrganisaatioFn(page: Page) {
    const locators = {
        yrityksenNimi: page.locator('[data-test-id="yrityksen-nimi"]'),
        ytunnus: page.locator('[data-test-id="ytunnus"]'),
        organisaatiotyyppi: page.locator('[data-test-id="organisaatiotyyppi"]'),
        postitoimipaikka: page.locator('[data-test-id="postitoimipaikka"]'),
        copyKayntiosoite: page.locator('#copyKayntiosoite'),
        addEmailButton: page.locator('[data-test-id="add-email"]'),
        error: (inputId: string) => page.locator(`[data-test-id="error-${inputId}"]`),
    };

    async function selectYritysmuoto(yritysmuoto = 'Asunto-osuuskunta') {
        await selectValue(page, 'yritysmuoto', yritysmuoto);
    }

    async function selectKotipaikka(kotipaikka = 'Akaa') {
        await selectValue(page, 'kotipaikka', kotipaikka);
    }

    async function fillAlkamisaika(alkamisaika: string = '1.1.2057') {
        await page.fill('#alkamisaika', alkamisaika);
    }

    async function fillPuhelinnumero(puhelinnumero: string = '123456789') {
        await page.fill('#puhelinnumero', puhelinnumero);
    }

    async function fillEmail(email: string = 'opiskelia@oph.fi') {
        await page.fill('#email', email);
    }

    async function fillPostiosoite(postiosoite: string = 'Hietaniemenkatu 14') {
        await page.fill('#postiosoite', postiosoite);
    }

    async function fillPostinumero(postinumero: string = '00200') {
        await page.fill('#postinumero', postinumero);
    }

    async function setKopioiKayntiosoite(copyKayntiosoite: boolean = true) {
        return copyKayntiosoite ? await locators.copyKayntiosoite.check() : await locators.copyKayntiosoite.uncheck();
    }

    async function fillKayntiosoite(kayntiosoite: string = 'Aurakatu 1') {
        await page.fill('#kayntiosoite', kayntiosoite);
    }

    async function fillKayntipostinumero(kayntipostinumero: string = '20100') {
        await page.fill('#kayntipostinumero', kayntipostinumero);
    }

    async function fillEmails(emails: string[] = ['toinen.opiskelia@oph.fi', 'kolmas.opiskelia@oph.fi']) {
        const emailInputs = page.locator('[aria-labelledby="email-label"]');
        const inputCount = await emailInputs.count();
        for (let i = 0; i < emails.length; i++) {
            if (i >= inputCount) {
                await locators.addEmailButton.click();
            }
            const currentInputs = page.locator('[aria-labelledby="email-label"]');
            await currentInputs.nth(i).fill(emails[i]);
        }
    }

    async function fillEmailAt(at: number, email: string = 'opiskelia@oph.fi') {
        await page.fill(`[name="emails.${at}.email"]`, email);
    }

    async function removeEmailAt(at: number) {
        await page.click(`[id="remove-email-${at}"]`);
    }

    async function submit() {
        await page.locator('input[type="submit"]').click();
    }

    async function fillWithDefaults() {
        await selectYritysmuoto();
        await selectKotipaikka();
        await fillAlkamisaika();
        await fillPuhelinnumero();
        await fillEmail();
        await fillPostiosoite();
        await fillPostinumero();
        await setKopioiKayntiosoite();
        await fillEmails();
    }

    return {
        selectYritysmuoto,
        selectKotipaikka,
        fillAlkamisaika,
        fillPuhelinnumero,
        fillEmail,
        fillPostiosoite,
        fillPostinumero,
        setKopioiKayntiosoite,
        fillKayntiosoite,
        fillKayntipostinumero,
        fillEmails,
        fillEmailAt,
        removeEmailAt,
        fillWithDefaults,
        submit,
        locators,
    };
}
