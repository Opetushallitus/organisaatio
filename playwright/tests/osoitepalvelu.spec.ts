import { test, expect } from "@playwright/test";

test.describe("Osoitepalvelu", () =>
  test("has title", async ({ page }) => {
    await page.goto("http://localhost:3003/osoitteet");

    await expect(page).toHaveTitle(/Organisaatio/);
  })
);
