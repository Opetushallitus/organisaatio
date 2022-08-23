import { Page } from '@playwright/test';

import { navigate } from '../util/navigate';

export function VardaLandingFn(page: Page) {
    async function goto() {
        await navigate(page, '/rekisterointi/varda');
        await page.waitForSelector('#kielivalikko');
    }

    const changeLanguage = async (lang: 'fi' | 'sv') => {
        await page.locator('#kielivalikko').selectOption(lang);
    };

    return {
        goto,
        changeLanguage,
    };
}
