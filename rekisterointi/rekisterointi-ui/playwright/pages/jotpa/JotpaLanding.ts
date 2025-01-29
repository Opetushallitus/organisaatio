import { Page } from '@playwright/test';

import { Language } from '../../../src/types';
import { navigate } from '../../util/navigate';

export function JotpaLandingFn(page: Page) {
    async function goto() {
        await navigate(page, '/jotpa');
        await page.waitForSelector('.content');
    }

    async function startRegistration() {
        await page.locator('button[role="link"]').click();
    }

    async function changeLanguage(lang: Language) {
        // todo
    }

    return {
        goto,
        startRegistration,
        changeLanguage,
    };
}
