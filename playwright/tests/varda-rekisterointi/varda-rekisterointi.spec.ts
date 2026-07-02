import { test, expect, type Page, type Route } from "@playwright/test";

async function useVardaMockApi(page: Page) {
  const routeToMockApi = async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const headers = request.headers();
    delete headers.host;

    const response = await page.request.fetch(
      `http://localhost:9000${url.pathname}${url.search}`,
      {
        method: request.method(),
        headers,
        data: request.postData() ?? undefined,
        failOnStatusCode: false,
      },
    );

    await route.fulfill({ response });
  };

  await page.route("**/varda-rekisterointi/api/**", routeToMockApi);
  await page.route(
    "**/varda-rekisterointi/virkailija/api/**",
    routeToMockApi,
  );
}

test.describe("Varda rekisterointi", () => {
  test("should show the registration button", async ({ page }) => {
    await page.goto("http://localhost:3000/varda-rekisterointi");
    await page.waitForSelector(':has-text("Aloita rekisteröityminen")');
  });

  test("accepts multiple registrations at once", async ({ page }) => {
    await useVardaMockApi(page);

    await page.goto("http://localhost:3000/varda-rekisterointi/virkailija");

    await expect(page.getByText("Jotpa-yritys", { exact: true })).toBeVisible();
    await expect(page.getByText("Jotpa-yritys 2")).toBeVisible();
    await expect(page.getByText("Jotpa-yritys 3")).toBeVisible();

    await page.locator('thead input[type="checkbox"]').check({ force: true });

    const approveSelected = page.getByRole("button", {
      name: /Hyv\u00e4ksy valitut \(3\)/,
    });
    await expect(approveSelected).toBeEnabled();

    await approveSelected.click();

    const dialog = page.getByRole("dialog");
    await expect(dialog).toContainText("Jotpa-yritys");
    await expect(dialog).toContainText("Jotpa-yritys 2");
    await expect(dialog).toContainText("Jotpa-yritys 3");

    const batchRequestPromise = page.waitForRequest(
      (request) =>
        request.method() === "POST" &&
        request
          .url()
          .includes("/varda-rekisterointi/virkailija/api/paatokset/batch"),
    );

    await dialog.getByRole("button", { name: "Hyv\u00e4ksy hakemus" }).click();

    const batchRequest = await batchRequestPromise;
    const batchBody = batchRequest.postDataJSON() as {
      hyvaksytty: boolean;
      hakemukset: number[];
    };

    expect(batchBody.hyvaksytty).toBe(true);
    expect([...batchBody.hakemukset].sort((a, b) => a - b)).toEqual([
      1001, 1006, 1007,
    ]);

    await expect(
      page.getByText("Rekister\u00f6innit hyv\u00e4ksytty"),
    ).toBeVisible();

    await page.getByRole("button", { name: "Hyv\u00e4ksytty" }).click();
    await expect(page.getByText("Jotpa-yritys", { exact: true })).toBeVisible();
    await expect(page.getByText("Jotpa-yritys 2")).toBeVisible();
    await expect(page.getByText("Jotpa-yritys 3")).toBeVisible();
  });
});
