import {Page} from "@playwright/test";

// Original source, Lexical E2E tests:
// https://github.com/facebook/lexical/blob/6b4c1dc8ab0169e64e534c8ad91df06244d27f82/packages/lexical-playground/__tests__/keyboardShortcuts/index.mjs#L141-L159

export async function selectAll(page: Page) {
  await keyDownCtrlOrMeta(page);
  await page.keyboard.press('a');
  await keyUpCtrlOrMeta(page);
}

export async function keyDownCtrlOrMeta(page) {
  if (await isMac(page)) {
    await page.keyboard.down('Meta');
  } else {
    await page.keyboard.down('Control');
  }
}

export async function keyUpCtrlOrMeta(page) {
  if (await isMac(page)) {
    await page.keyboard.up('Meta');
  } else {
    await page.keyboard.up('Control');
  }
}

export async function isMac(page) {
  return page.evaluate(
    () =>
      typeof window !== 'undefined' &&
      /Mac|iPod|iPhone|iPad/.test(window.navigator.platform),
  );
}

