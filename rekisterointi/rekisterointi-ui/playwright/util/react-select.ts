import { Page } from '@playwright/test';

export async function selectValue(page: Page, selectId: string, value: string) {
    const inputContainer = page.locator(`div > #select-${selectId}`);
    await inputContainer.click();
    const option = page.locator(`div:text("${value}")`);
    await option.scrollIntoViewIfNeeded();
    await option.click();
}
