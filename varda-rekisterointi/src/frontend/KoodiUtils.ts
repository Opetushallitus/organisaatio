import { Koodi, Language } from './types/types';
import { toLocalizedText } from './LocalizableTextUtils';

export function toLocalizedKoodi(koodi: Koodi | null | undefined, language: Language, defaultValue?: string): string {
    if (koodi === null || koodi === undefined) {
        return defaultValue || '';
    }
    return toLocalizedText(koodi.nimi, language, defaultValue);
}
