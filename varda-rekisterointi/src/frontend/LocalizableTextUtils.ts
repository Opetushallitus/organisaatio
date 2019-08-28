import { Language, LocalizableText } from "./types";
import { hasLength } from "./StringUtils";

export function toLocalizedText(localizableText: LocalizableText | null | undefined,
                                language: Language,
                                defaultValue?: string): string {
    if (localizableText) {
        let localizedText = localizableText[language] || localizableText.fi || localizableText.sv || localizableText.en;
        if (localizedText) {
            return localizedText;
        }
    }
    return defaultValue || '';
}

export function hasLengthInLang(localizableText: LocalizableText | null | undefined,
                                language: Language) {
    if (localizableText === null || localizableText === undefined) {
        return false;
    }
    return hasLength(localizableText[language]);
}

export const asiointikielet = [
    {value: 'fi', label: {fi: 'suomi', sv: 'finska', en: 'Finnish'}},
    {value: 'sv', label: {fi: 'ruotsi', sv: 'svenska', en: 'Swedish'}},
    {value: 'en', label: {fi: 'englanti', svn: 'engelska', en: 'English'}},
];
