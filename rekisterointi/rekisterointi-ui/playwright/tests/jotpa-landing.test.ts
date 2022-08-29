import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

import { JotpaLandingFn } from '../pages/JotpaLanding';

test('Jotpa landing page', async ({ page }) => {
    const JotpaLanding = JotpaLandingFn(page);
    await JotpaLanding.goto();

    await test.step('is accessible', async () => {
        const results = await new AxeBuilder({ page }).analyze();
        expect(results.violations).toEqual([]);
    });
});
