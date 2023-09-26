import {expect, test} from "@playwright/test";

test.describe("Osoitepalvelu", () => {
    test("has title", async ({page}) => {
        await page.goto("http://localhost:3003/osoitteet");

        await expect(page).toHaveTitle(/Organisaatio/);
    });
    test("allows searching for koulutustoimijat", async ({page}) => {
        await page.goto("http://localhost:3003/osoitteet");

        await expect(page.getByText("Hae")).toBeVisible();
        await page.getByText("Hae").click();

        await expect(page.getByText("3 hakutulosta valittu")).toBeVisible();
        await expect(page.getByText("Mansikkalan testi kunta")).toBeVisible();
        await expect(page.getByText("Testi Koulutuskuntayhtymä Puolukka")).toBeVisible();
        await expect(page.getByText("Mustikkalan testi yhdistys")).toBeVisible();
    });
});
