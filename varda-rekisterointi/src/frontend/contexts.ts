import React from 'react';
import { Koodi, KoodiArvo, KoodiUri, Language, Lokalisointi, Permission } from './types/types';

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

export const LanguageContext = React.createContext<LanguageContextType>({
    language: 'fi',
    setLanguage: (language: Language) => {},
    i18n: new I18nImpl({ fi: {}, sv: {}, en: {} }, 'fi'),
});

export const PermissionContext = React.createContext<Permission>({
    hasCreatePermission: false,
    registrationTypes: [],
});

export interface Koodisto {
    uri2Nimi: (uri: KoodiUri) => string;
    arvo2Nimi: (arvo: KoodiArvo) => string;
    nimet: () => string[];
    koodit: () => Koodi[];
}

export class KoodistoImpl implements Koodisto {
    constructor(private readonly koodisto: Koodi[], private readonly kieli: Language) {}

    uri2Nimi(uri: KoodiUri): string {
        return this.nimi((koodi) => koodi.uri === uri);
    }

    arvo2Nimi(arvo: KoodiArvo): string {
        return this.nimi((koodi) => koodi.arvo === arvo);
    }

    nimet(): string[] {
        return this.koodisto.map((koodi) => this.kielistettyNimi(koodi));
    }

    koodit(): Koodi[] {
        return [...this.koodisto];
    }

    private nimi(predikaatti: (koodi: Koodi) => boolean): string {
        const koodi = this.koodisto.find(predikaatti);
        let nimi = '';
        if (koodi) {
            nimi = this.kielistettyNimi(koodi);
        }
        return nimi;
    }

    private kielistettyNimi(koodi: Koodi): string {
        return koodi.nimi[this.kieli] || (this.kieli === 'fi' ? '' : koodi.nimi['fi'] || '');
    }
}

type KoodistoContextType = {
    koodisto: Koodisto;
};

export const KuntaKoodistoContext = React.createContext<KoodistoContextType>({
    koodisto: new KoodistoImpl([], 'fi'),
});

export const MaatJaValtiotKoodistoContext = React.createContext<KoodistoContextType>({
    koodisto: new KoodistoImpl([], 'fi'),
});
