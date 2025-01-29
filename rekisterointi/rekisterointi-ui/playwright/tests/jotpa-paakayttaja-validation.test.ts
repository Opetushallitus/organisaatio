import { expect } from '@playwright/test';

import { test } from '../fixtures/jotpa';

test('Paakayttaja validation', async ({ pages }) => {
    const { landing, organisaatio, paakayttaja } = pages;

    await landing.goto();
    await landing.startRegistration();

    await organisaatio.fillWithDefaults();
    await organisaatio.submit();

    await paakayttaja.locators.form.waitFor({ state: 'visible' });
    await paakayttaja.submit();

    await expect(paakayttaja.locators.error('etunimi')).toHaveText('Pakollinen tieto');
    await expect(paakayttaja.locators.error('sukunimi')).toHaveText('Pakollinen tieto');
    await expect(paakayttaja.locators.error('paakayttajaEmail')).toHaveText('Pakollinen tieto');
    await expect(paakayttaja.locators.error('asiointikieli')).toHaveText('Pakollinen tieto');

    await paakayttaja.fillEtunimi();
    await expect(paakayttaja.locators.error('etunimi')).toBeHidden();

    await paakayttaja.fillSukunimi();
    await expect(paakayttaja.locators.error('sukunimi')).toBeHidden();

    await paakayttaja.fillEmail('sahkoposti');
    await expect(organisaatio.locators.error('paakayttajaEmail')).toHaveText('Virheellinen sähköposti');
    await paakayttaja.fillEmail();
    await expect(organisaatio.locators.error('paakayttajaEmail')).toBeHidden();

    await paakayttaja.selectAsiointikieli('sv');
    await expect(organisaatio.locators.error('asiointikieli')).toBeHidden();
});
