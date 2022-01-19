import { I18n, Language, Lokalisointi, Nimi } from '../types/types';
import * as React from 'react';

export class I18nImpl implements I18n {
    _data: Lokalisointi;
    _language: Language;

    constructor(data: Lokalisointi, language: Language) {
        this._data = data;
        this._language = language;
    }

    translate(key: string): string {
        return this.translateWithLang(key, this._language);
    }

    translateWithLang(key: string, language: Language): string {
        const translation = this._data[language]?.[key];
        if (!translation) {
            console.info(`Translation is missing for ${key} in language ${language}`);
            return key;
        }
        return translation;
    }

    translateNimi = (nimi: Nimi | undefined) => {
        return (nimi && (nimi[this._language] || nimi['fi'] || nimi['sv'] || nimi['en'])) || '';
    };

    enrichMessage = (key: string, replacements: { key: string; value: string }[]) => {
        return replacements.reduce((previous, current) => {
            return previous.replace(`{${current.key}}`, current.value);
        }, this.translate(key));
    };
}

export type LanguageContextType = {
    language: Language;
    i18n: I18n;
};
export const LanguageContext = React.createContext<LanguageContextType>({
    language: 'fi',
    i18n: new I18nImpl({ fi: {}, sv: {}, en: {} }, 'fi'),
});
