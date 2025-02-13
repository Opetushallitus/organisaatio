import { defineConfig, devices } from "@playwright/test";
const CI = !!process.env.CI || !!process.env.CODEBUILD_BUILD_ID;
export default defineConfig({
  testDir: "./tests",
  fullyParallel: true,
  forbidOnly: CI,
  retries: CI ? 2 : 0,
  workers: 2,
  reporter: CI ? [["junit", { outputFile: "playwright-results.xml" }]] : "html",
  use: {
    trace: "on",
    httpCredentials: {
      username: "devaaja",
      password: "devaaja",
    },
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
