import { expect } from '@playwright/test';

import { test } from '../fixtures/jotpa';
import { headingOrderId, removeExpectViolations } from '../util/accessability';

test('Jotpa accessability', async ({ pages, page, axe }) => {
    const { landing, organisaatio, paakayttaja, yhteenveto, valmis } = pages;

    await landing.goto();

    await test.step('landing is accessible', async () => {
        const results = await axe.analyze();
        expect(results.violations).toEqual([]);
    });

    await landing.startRegistration();
    await organisaatio.locators.yrityksenNimi.waitFor({ state: 'visible' });

    await test.step('organisaatio is accessible', async () => {
        const results = await axe.analyze();
        expect(results.violations).toEqual([]);
    });

    await organisaatio.submit();

    await test.step('organisaatio with error messages is accessible', async () => {
        const results = await axe.analyze();
        expect(results.violations).toEqual([]);
    });

    await organisaatio.fillWithDefaults();
    await organisaatio.submit();
    await paakayttaja.locators.form.waitFor({ state: 'visible' });

    await test.step('paakayttaja is accessible', async () => {
        const results = await axe.analyze();
        expect(results.violations).toEqual([]);
    });

    await paakayttaja.submit();

    await test.step('paakayttaja with error messages is accessible', async () => {
        const results = await axe.analyze();
        expect(results.violations).toEqual([]);
    });

    await paakayttaja.fillWithDefaults();
    await paakayttaja.submit();
    await yhteenveto.locators.yrityksenNimi.waitFor({ state: 'visible' });

    await test.step('yhteenveto is accessible', async () => {
        const results = await axe.analyze();
        expect(results.violations).toEqual([]);
    });

    await yhteenveto.submit();
    await valmis.locators.content.waitFor({ state: 'visible' });

    await test.step('valmis is accessible', async () => {
        const results = await axe.analyze();
        const violations = removeExpectViolations(results.violations, [headingOrderId]);
        expect(violations).toEqual([]);
    });
});
