import { Page } from '@playwright/test';

import { navigate } from '../util/navigate';

export function JotpaLandingFn(page: Page) {
    async function goto() {
        await navigate(page, '/jotpa');
        await page.waitForSelector('.content');
    }

    const changeLanguage = async (lang: 'fi' | 'sv') => {
        // todo
    };

    return {
        goto,
        changeLanguage,
    };
}
