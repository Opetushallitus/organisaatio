import React from 'react';
import { Language } from './types';

export interface I18n {
    translate: (key: string) => string,
}

export class I18nImpl implements I18n {

    _data: Record<string, string>;

    constructor(data: Record<string, string>) {
        this._data = data;
    }

    translate(key: string): string {
        return this._data[key] || key;
    }

}

type LanguageContextType = {
    language: Language,
    setLanguage: (language: Language) => void,
    i18n: I18n,
}

export const LanguageContext = React.createContext<LanguageContextType>({
    language: 'fi',
    setLanguage: (language: Language) => {},
    i18n: new I18nImpl({}),
});
