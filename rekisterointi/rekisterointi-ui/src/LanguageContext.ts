import React, { useContext } from 'react';
import { Language, Lokalisointi } from './types';

export interface I18n {
    translate: (key: string) => string;
    translateWithLang: (key: string, language: Language) => string;
}

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
        return this._data[language][key] || key;
    }
}

type LanguageContextType = {
    language: Language;
    setLanguage: (language: Language) => void;
    i18n: I18n;
};

export const defaultLokalisointi = { fi: {}, sv: {}, en: {} };

export const LanguageContext = React.createContext<LanguageContextType>({
    language: 'fi',
    setLanguage: (language: Language) => {},
    i18n: new I18nImpl(defaultLokalisointi, 'fi'),
});

export const useLanguageContext = () => {
    const context = useContext(LanguageContext);
    if (!context && context !== null) {
        throw new Error('Localization not available');
    }
    return context;
};
