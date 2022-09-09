import { Page } from '@playwright/test';

export function JotpaValmisFn(page: Page) {
    const path = '/jotpa/valmis';

    const locators = {
        heading: page.locator('h1'),
        content: page.locator('[data-test-id="valmis-content"]'),
    };

    return {
        path,
        locators,
    };
}
