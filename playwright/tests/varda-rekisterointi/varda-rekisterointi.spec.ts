import { test, expect } from "@playwright/test";

test.describe("Varda rekisterointi", () => {
  test("should show the registration button", async ({ page }) => {
    await page.goto("http://localhost:3000/varda-rekisterointi");
    await page.waitForSelector(':has-text("Aloita rekisteröityminen")');
  });
});
