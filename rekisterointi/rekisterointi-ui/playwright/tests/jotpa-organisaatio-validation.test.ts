import { expect } from '@playwright/test';

import { test } from '../fixtures/jotpa';

test.describe('Organisaatio validation', () => {
    test('Basic validation', async ({ pages }) => {
        const { landing, organisaatio } = pages;

        await landing.goto();
        await landing.startRegistration();

        await organisaatio.submit();

        await expect(organisaatio.locators.error('yritysmuoto')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('kotipaikka')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('alkamisaika')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('puhelinnumero')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('email')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('postiosoite')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('postinumero')).toHaveText('Virheellinen postinumero');
        await expect(organisaatio.locators.error('kayntiosoite')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('kayntipostinumero')).toHaveText('Virheellinen postinumero');
        await expect(organisaatio.locators.error('email-0')).toHaveText('Pakollinen tieto');

        await organisaatio.selectYritysmuoto();
        await expect(organisaatio.locators.error('yritysmuoto')).toBeHidden();

        await organisaatio.selectKotipaikka();
        await expect(organisaatio.locators.error('kotipaikka')).toBeHidden();

        await organisaatio.fillAlkamisaika();
        await expect(organisaatio.locators.error('alkamisaika')).toBeHidden();

        await organisaatio.fillPuhelinnumero('12');
        await expect(organisaatio.locators.error('puhelinnumero')).toHaveText('Virheellinen arvo');
        await organisaatio.fillPuhelinnumero();
        await expect(organisaatio.locators.error('puhelinnumero')).toBeHidden();

        await organisaatio.fillEmail('sahkoposti');
        await expect(organisaatio.locators.error('email')).toHaveText('Virheellinen sähköposti');
        await organisaatio.fillEmail();
        await expect(organisaatio.locators.error('email')).toBeHidden();

        await organisaatio.fillPostiosoite();
        await expect(organisaatio.locators.error('postiosoite')).toBeHidden();

        await organisaatio.fillPostinumero('12345');
        await expect(organisaatio.locators.error('postinumero')).toHaveText('Virheellinen postinumero');
        await organisaatio.fillPostinumero();
        await expect(organisaatio.locators.error('postinumero')).toBeHidden();

        await organisaatio.fillKayntiosoite();
        await expect(organisaatio.locators.error('kayntiosoite')).toBeHidden();

        await organisaatio.fillKayntipostinumero('12345');
        await expect(organisaatio.locators.error('kayntipostinumero')).toHaveText('Virheellinen postinumero');
        await organisaatio.fillKayntipostinumero();
        await expect(organisaatio.locators.error('kayntipostinumero')).toBeHidden();

        await organisaatio.fillEmails(['email@oph.fi']);
        await expect(organisaatio.locators.error('email-0')).toBeHidden();
    });

    test('Multiple emails', async ({ pages }) => {
        const { landing, organisaatio } = pages;

        await landing.goto();
        await landing.startRegistration();

        await organisaatio.locators.addEmailButton.click();
        await organisaatio.locators.addEmailButton.click();

        await organisaatio.submit();

        await expect(organisaatio.locators.error('email-0')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('email-1')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('email-2')).toHaveText('Pakollinen tieto');

        await organisaatio.fillEmailAt(0, 'sahkoposti');
        await expect(organisaatio.locators.error('email-0')).toHaveText('Virheellinen sähköposti');
        await expect(organisaatio.locators.error('email-1')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('email-2')).toHaveText('Pakollinen tieto');

        await organisaatio.fillEmailAt(0);
        await expect(organisaatio.locators.error('email-0')).toBeHidden();
        await expect(organisaatio.locators.error('email-1')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('email-2')).toHaveText('Pakollinen tieto');

        await organisaatio.removeEmailAt(1);
        await expect(organisaatio.locators.error('email-0')).toBeHidden();
        await expect(organisaatio.locators.error('email-1')).toHaveText('Pakollinen tieto');
        await expect(organisaatio.locators.error('email-2')).toBeHidden();

        await organisaatio.fillEmailAt(1);
        await expect(organisaatio.locators.error('email-0')).toBeHidden();
        await expect(organisaatio.locators.error('email-1')).toBeHidden();
        await expect(organisaatio.locators.error('email-2')).toBeHidden();
    });
});
