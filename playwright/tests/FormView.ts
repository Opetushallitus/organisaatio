import { Page } from "@playwright/test";
import { selectAll } from "./LexicalUtil";

export class FormView {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async fillInput(name: string, text: string) {
    await this.page.locator(`input[name="${name}"]`).focus();
    await selectAll(this.page);
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(text);
  }

  async openDropdown(label: string) {
    await this.page.locator(`div:has(> [aria-label="${label}"])`).click();
  }
}
