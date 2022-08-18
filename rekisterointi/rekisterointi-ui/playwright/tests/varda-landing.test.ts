import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

import { VardaLandingFn } from '../pages/VardaLanding';

test('Varda landing page', async ({ page }) => {
    const VardaLanding = VardaLandingFn(page);
    await VardaLanding.goto();

    await test.step('is accessible', async () => {
        const results = await new AxeBuilder({ page }).analyze();
        expect(results.violations).toEqual([]);
    });

    await test.step('can change language', async () => {
        const heading = page.locator('h1');
        expect(await heading.innerText()).toEqual('Mikä on Varda?');
        await VardaLanding.changeLanguage('sv');
        expect(await heading.innerText()).toEqual('Vad är Varda?');
    });
});
