import { expect } from '@playwright/test';

import { test } from '../fixtures/jotpa';

test('Jotpa happy path', async ({ pages, page }) => {
    const { landing, organisaatio, paakayttaja, yhteenveto, valmis } = pages;

    await landing.goto();
    await landing.startRegistration();

    await expect(organisaatio.locators.yrityksenNimi).toHaveText('Meyer Turku Oy');
    await expect(organisaatio.locators.ytunnus).toHaveText('0772017-4');
    await expect(organisaatio.locators.organisaatiotyyppi).toHaveText('Koulutustoimija');

    await organisaatio.fillWithDefaults();
    await expect(organisaatio.locators.postitoimipaikka).toHaveText('HELSINKI');
    await organisaatio.submit();

    await paakayttaja.fillWithDefaults();
    await paakayttaja.submit();

    await expect(yhteenveto.locators.yrityksenNimi).toHaveText('Meyer Turku Oy');
    await expect(yhteenveto.locators.ytunnus).toHaveText('0772017-4');
    await expect(yhteenveto.locators.yritysmuoto).toHaveText('Asunto-osuuskunta');
    await expect(yhteenveto.locators.organisaatiotyyppi).toHaveText('Koulutustoimija');
    await expect(yhteenveto.locators.kotipaikka).toHaveText('Akaa');
    await expect(yhteenveto.locators.alkamisaika).toHaveText('1.1.2057');
    await expect(yhteenveto.locators.puhelinnumero).toHaveText('123456789');
    await expect(yhteenveto.locators.organisaatioEmail).toHaveText('opiskelia@oph.fi');
    await expect(yhteenveto.locators.postiosoite).toHaveText('Hietaniemenkatu 14');
    await expect(yhteenveto.locators.postinumero).toHaveText('00200');
    await expect(yhteenveto.locators.postitoimipaikka).toHaveText('HELSINKI');
    await expect(yhteenveto.locators.kayntiosoite).toHaveText('Hietaniemenkatu 14');
    await expect(yhteenveto.locators.kayntipostinumero).toHaveText('00200');
    await expect(yhteenveto.locators.kayntipostitoimipaikka).toHaveText('HELSINKI');
    await expect(yhteenveto.locators.emails).toHaveText('toinen.opiskelia@oph.fikolmas.opiskelia@oph.fi');
    await expect(yhteenveto.locators.etunimi).toHaveText('Etu');
    await expect(yhteenveto.locators.sukunimi).toHaveText('Suku');
    await expect(yhteenveto.locators.paakayttajaEmail).toHaveText('paakayttaja@oph.fi');
    await expect(yhteenveto.locators.asiointikieli).toHaveText('Suomi');
    await expect(yhteenveto.locators.saateteksti).toHaveText('Saateteksti');

    //await yhteenveto.submit();

    //expect(page.url()).toContain(valmis.path);
    //await expect(valmis.locators.heading).toHaveText('Rekisteröinti lähetetty');
});
