import {Locator, Page} from "@playwright/test";
import {selectAll} from "./LexicalUtil";

export class ViestiView {
  readonly page: Page;
  readonly lahetaButton: Locator;
  readonly aiheField: FormField;
  readonly viestiField: ViestiField;
  readonly replyToField: FormField;
  readonly fileUploadButton: Locator;

  constructor(page: Page) {
    this.lahetaButton = page.getByRole("button", { name: "Lähetä" });
    this.aiheField = new AiheField(page);
    this.viestiField = new ViestiField(page);
    this.replyToField = new ReplyToField(page);
    this.fileUploadButton = page.getByLabel("Lataa liitetiedosto");
  }
}

export class FormField {
  readonly page: Page;
  readonly context: Locator;
  readonly input: Locator;
  readonly errorFeedback: Locator;
  constructor(page: Page, input: Locator) {
    this.page = page;
    this.input = input;
    this.context = page.getByRole("group").filter({ has: input });
    this.errorFeedback = this.context.locator("p.error");
  }
}

class AiheField extends FormField {
  constructor(page: Page) {
    const input = page.getByLabel("Aihe*");
    super(page, input);
  }
}

class ViestiField extends FormField {
  constructor(page: Page) {
    const input = page.locator('div[contenteditable="true"]');
    super(page, input);
  }

  async fill(text: string) {
    await this.input.focus();
    await this.clear();
    await this.page.keyboard.type(text)
  }

  async clear() {
    await selectAll(this.page)
    await this.page.keyboard.press("Backspace");
    await this.page.keyboard.press("Backspace");
  }
}

class ReplyToField extends FormField {
  constructor(page: Page) {
    const input = page.getByLabel("Vastausosoite (reply-to)");
    super(page, input);
  }
}
