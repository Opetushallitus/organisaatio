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

  async selectFromDropdown(id: string, value: string) {
    await this.page.locator(`#${id} input[type=text]`).focus();
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(value);
    await this.page.keyboard.press("Enter");
  }

  async setDate(label: string, date: string) {
    await this.page.getByText(label).locator("..").locator("input").focus();
    await selectAll(this.page);
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.type(date);
  }
}
