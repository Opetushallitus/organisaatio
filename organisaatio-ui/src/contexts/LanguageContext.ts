import { I18n, Language, Lokalisointi, Nimi } from '../types/types';

export class I18nImpl implements I18n {
    _data: Lokalisointi;
    _language: Language;

    constructor(data: Lokalisointi, language: Language) {
        this._data = data;
        this._language = language;
    }

    translate(key: string, keyIfEmpty = true): string {
        return this.translateWithLang(key, this._language, keyIfEmpty);
    }

    translateWithLang(key: string, language: Language, keyIfEmpty = true): string {
        const translation = this._data[language]?.[key];
        !translation &&
            process.env.NODE_ENV !== 'development' &&
            console.info(`Translation is missing for ${key} in language ${language}`);
        if (translation) return translation;
        return keyIfEmpty ? key : '';
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
