import { Language, LocalizableText, KoodiUri } from './types/types';
import { isNonEmpty } from './StringUtils';

export function toLocalizedText(
    localizableText: LocalizableText | null | undefined,
    language: Language,
    defaultValue?: string
): string {
    if (localizableText) {
        let localizedText = localizableText[language] || localizableText.fi || localizableText.sv || localizableText.en;
        if (localizedText) {
            return localizedText;
        }
    }
    return defaultValue || '';
}

export function hasLengthInLang(localizableText: LocalizableText | null | undefined, language: Language) {
    if (localizableText === null || localizableText === undefined) {
        return false;
    }
    return isNonEmpty(localizableText[language]);
}

export const asiointikielet = [
    { value: 'fi', label: { fi: 'suomi', sv: 'finska', en: 'Finnish' } },
    { value: 'sv', label: { fi: 'ruotsi', sv: 'svenska', en: 'Swedish' } },
];

export function ytjKieliToLanguage(ytjKieli: KoodiUri) {
    switch (ytjKieli) {
        case 'kieli_fi#1':
            return 'fi';
        case 'kieli_sv#1':
            return 'sv';
        case 'kieli_en#1':
            return 'en';
        default:
            throw new Error('Unsupported language');
    }
}
