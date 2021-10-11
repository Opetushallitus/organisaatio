import * as React from 'react';
import organisaatioRakenne from './organisaatioRakenne.json';
import { Koodi, KoodiArvo, KoodiUri, Language, Lokalisointi, Nimi } from '../types/types';

export const ROOT_OID = '1.2.246.562.10.00000000001';
export const rakenne = organisaatioRakenne;
export interface I18n {
    translate: (key: string) => string;
    translateWithLang: (key: string, language: Language) => string;
    translateNimi: (nimi: Nimi | undefined) => string;
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

    translateNimi = (nimi: Nimi | undefined) => {
        return (nimi && (nimi[this._language] || nimi['fi'] || nimi['sv'] || nimi['en'])) || '';
    };
}

type LanguageContextType = {
    language: Language;
    i18n: I18n;
};

export const LanguageContext = React.createContext<LanguageContextType>({
    language: 'fi',
    i18n: new I18nImpl({ fi: {}, sv: {}, en: {} }, 'fi'),
});

export interface Koodisto {
    uri2Nimi: (uri: KoodiUri) => string;
    arvo2Nimi: (arvo: KoodiArvo) => string;
    nimet: () => string[];
    koodit: () => Koodi[];
}

export class KoodistoImpl implements Koodisto {
    private readonly koodisto: Koodi[];
    private readonly kieli: Language;

    constructor(koodisto: Koodi[], kieli: Language) {
        this.koodisto = koodisto;
        this.kieli = kieli;
    }

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
    kuntaKoodisto: Koodisto;
    kayttoRyhmatKoodisto: Koodisto;
    ryhmaTyypitKoodisto: Koodisto;
    organisaatioTyypitKoodisto: Koodisto;
    ryhmanTilaKoodisto: Koodisto;
};

export const KoodistoContext = React.createContext<KoodistoContextType>({
    kuntaKoodisto: new KoodistoImpl([], 'fi'),
    kayttoRyhmatKoodisto: new KoodistoImpl([], 'fi'),
    ryhmaTyypitKoodisto: new KoodistoImpl([], 'fi'),
    organisaatioTyypitKoodisto: new KoodistoImpl([], 'fi'),
    ryhmanTilaKoodisto: new KoodistoImpl([], 'fi'),
});
