import { Page, test as originalTest } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

type BaseTest = {
    page: Page;
    axe: AxeBuilder;
};

export const test = originalTest.extend<BaseTest>({
    page: async ({ browser }, use) => {
        const context = await browser.newContext({
            httpCredentials: {
                username: 'dev',
                password: 'dev',
            },
        });
        const page = await context.newPage();
        await use(page);
    },
    axe: async ({ page }, use) => {
        const axe = new AxeBuilder({ page });
        use(axe);
    },
});
