import { Locator, Page } from '@playwright/test';

import { REKISTEROINTI_URL } from './constants';

export async function navigate(page: Page, path?: string) {
    return await page.goto(`${REKISTEROINTI_URL}${path ?? ''}`);
}

async function waitForNewTab(currentPage: Page): Promise<Page> {
    return new Promise((resolve) => currentPage.once('popup', (newPage) => resolve(newPage)));
}

export async function openLinkInNewTab(page: Page, locator: Locator): Promise<Page> {
    const [newTab] = await Promise.all([waitForNewTab(page), await locator.click()]);
    return newTab;
}
