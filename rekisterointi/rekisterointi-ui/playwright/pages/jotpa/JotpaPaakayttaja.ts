import { Page } from '@playwright/test';

import { Language } from '../../../src/types';

export function JotpaPaakayttajaFn(page: Page) {
    const locators = {
        form: page.locator('[data-test-id="paakayttaja-form"]'),
        error: (inputId: string) => page.locator(`[data-test-id="error-${inputId}"]`),
    };

    async function fillEtunimi(etunimi: string = 'Etu') {
        await page.fill('#etunimi', etunimi);
    }

    async function fillSukunimi(sukunimi: string = 'Suku') {
        await page.fill('#sukunimi', sukunimi);
    }

    async function fillEmail(email: string = 'paakayttaja@oph.fi') {
        await page.fill('#paakayttajaEmail', email);
    }

    async function selectAsiointikieli(language: Language = 'fi') {
        await page.click(`#${language}`);
    }

    async function fillSaateteksti(postiosoite: string = 'Saateteksti') {
        await page.fill('#info', postiosoite);
    }

    async function submit() {
        await page.locator('input[type="submit"]').click();
    }

    async function fillWithDefaults() {
        await fillEtunimi();
        await fillSukunimi();
        await fillEmail();
        await selectAsiointikieli();
        await fillSaateteksti();
    }

    return {
        fillEtunimi,
        fillSukunimi,
        fillEmail,
        selectAsiointikieli,
        fillSaateteksti,
        fillWithDefaults,
        submit,
        locators,
    };
}
