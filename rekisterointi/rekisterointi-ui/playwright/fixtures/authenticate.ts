import { test as originalTest } from '@playwright/test';

export const test = originalTest.extend({
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
});
