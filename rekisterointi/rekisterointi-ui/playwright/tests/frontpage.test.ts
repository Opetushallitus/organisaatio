import { test, expect } from '@playwright/test';

import { FrontPageFn } from '../pages/frontpage';
import { openLinkInNewTab } from '../util/navigate';

const REACTJS_URL = 'https://reactjs.org';

test('Link opens correct url in new tab', async ({ page }) => {
    const FrontPage = FrontPageFn(page);
    await FrontPage.goto();
    expect(await FrontPage.link.getAttribute('href')).toEqual(REACTJS_URL);
    const newTab = await openLinkInNewTab(page, FrontPage.link);
    expect(newTab.url()).toEqual(`${REACTJS_URL}/`);
});

test('Correct file', async ({ page }) => {
    const FrontPage = FrontPageFn(page);
    await FrontPage.goto();
    const fileName = page.locator('code');
    expect(await fileName.textContent()).toEqual('src/App.js');
});
