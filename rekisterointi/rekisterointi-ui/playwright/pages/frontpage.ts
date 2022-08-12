import { Page } from '@playwright/test';

import { navigate } from '../util/navigate';

export function FrontPageFn(page: Page) {
    async function goto() {
        await navigate(page);
    }

    const link = page.locator('.App-link');

    return {
        goto,
        link,
    };
}
