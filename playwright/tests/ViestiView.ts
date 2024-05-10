import { expect, Locator, Page } from "@playwright/test";

export class ViestiView {
  readonly page: Page;
  readonly lahetaButton: Locator;
  readonly aiheField: FormField;
  readonly viestiField: FormField;
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
  readonly context: Locator;
  readonly input: Locator;
  readonly errorFeedback: Locator;
  constructor(page: Page, input: Locator) {
    this.input = input;
    this.context = page.getByRole("group").filter({ has: input });
    this.errorFeedback = this.context.locator("p");
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
    const input = page.locator("textarea");
    super(page, input);
  }
}

class ReplyToField extends FormField {
  constructor(page: Page) {
    const input = page.getByLabel("Vastausosoite (reply-to)");
    super(page, input);
  }
}
