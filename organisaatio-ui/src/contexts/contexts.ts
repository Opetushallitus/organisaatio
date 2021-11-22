import * as React from 'react';
import {
    I18n,
    Koodi,
    KoodiArvo,
    Koodisto,
    KoodistoContextType,
    KoodistoSelectOption,
    KoodiUri,
    Language,
    Lokalisointi,
    Nimi,
} from '../types/types';
import organisaatioRakenne from './organisaatioRakenne.json';
import { homepage } from '../../package.json';
export const ROOT_OID = '1.2.246.562.10.00000000001';

export const BASE_PATH = homepage;
export const API_CONTEXT = `${homepage}/internal`;
export const PUBLIC_API_CONTEXT = `${homepage}/api`;
export const LEGACY_API_CONTEXT = `/organisaatio-service/rest`;
export const rakenne = organisaatioRakenne;

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
        return this._data[language]?.[key] || key;
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

type LanguageContextType = {
    language: Language;
    i18n: I18n;
};

export const LanguageContext = React.createContext<LanguageContextType>({
    language: 'fi',
    i18n: new I18nImpl({ fi: {}, sv: {}, en: {} }, 'fi'),
});

export class KoodistoImpl implements Koodisto {
    private readonly koodisto: Koodi[];
    private readonly kieli: Language;
    private readonly KoodistoOptionValues: KoodistoSelectOption[];

    constructor(koodisto: Koodi[], kieli: Language) {
        this.koodisto = koodisto.sort((a, b) => a.uri.localeCompare(b.uri));
        this.kieli = kieli;
        this.KoodistoOptionValues = koodisto.map((koodi: Koodi) => this.uri2SelectOption(koodi.uri, koodi.versio));
    }

    uri2SelectOption(uri: KoodiUri, versio?: number): KoodistoSelectOption {
        const label = this.nimi((koodi) => uri?.startsWith(koodi.uri));
        return {
            value: label === '' ? label : `${uri}${versio ? `#${versio}` : ''}`,
            label,
        };
    }

    uri2Nimi(uri: KoodiUri): string {
        return this.nimi((koodi) => koodi.uri === uri);
    }

    uri2Arvo(uri: KoodiUri): string {
        return this.koodit().find((koodi) => koodi.uri === uri)?.arvo || '';
    }

    arvo2Nimi(arvo: KoodiArvo): string {
        return this.nimi((koodi) => koodi.arvo === arvo);
    }

    arvo2Uri(arvo: string): string {
        return this.koodit().find((koodi) => koodi.arvo === arvo)?.uri || '';
    }

    nimet(): string[] {
        return this.koodisto.map((koodi) => this.kielistettyNimi(koodi));
    }

    koodit(): Koodi[] {
        return [...this.koodisto];
    }

    selectOptions(): KoodistoSelectOption[] {
        return [...this.KoodistoOptionValues];
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

export const KoodistoContext = React.createContext<KoodistoContextType>({
    kuntaKoodisto: new KoodistoImpl([], 'fi'),
    kayttoRyhmatKoodisto: new KoodistoImpl([], 'fi'),
    ryhmaTyypitKoodisto: new KoodistoImpl([], 'fi'),
    organisaatioTyypitKoodisto: new KoodistoImpl([], 'fi'),
    ryhmanTilaKoodisto: new KoodistoImpl([], 'fi'),
    oppilaitoksenOpetuskieletKoodisto: new KoodistoImpl([], 'fi'),
    postinumerotKoodisto: new KoodistoImpl([], 'fi'),
    maatJaValtiotKoodisto: new KoodistoImpl([], 'fi'),
});
