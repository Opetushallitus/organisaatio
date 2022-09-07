import { expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

import { JotpaLandingFn } from '../pages/JotpaLanding';
import { test } from '../fixtures/authenticate';

test('Jotpa landing page', async ({ page }) => {
    const JotpaLanding = JotpaLandingFn(page);
    await JotpaLanding.goto();

    await test.step('is accessible', async () => {
        const results = await new AxeBuilder({ page }).analyze();
        expect(results.violations).toEqual([]);
    });
});
