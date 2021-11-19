import { ApiOrganisaatio, ApiYhteystiedot, OrganisaatioBase } from './apiTypes';
import * as React from 'react';

export type Language = 'fi' | 'sv' | 'en';
export type LocalDate = string;
export type LocalizableText = Partial<Record<Language, string>>;

// koodisto
export type KoodiUri = string;
export type KoodiArvo = string;
export type Koodi = {
    uri: KoodiUri;
    arvo: KoodiArvo;
    nimi: LocalizableText;
    versio: number;
};

export type KoodistoSelectOption = {
    value: KoodiUri;
    label: string;
};

// lokalisointi
export type Lokalisointi = Record<Language, Record<string, string>>;

export type Osoite = {
    osoite?: string;
    postinumeroUri?: KoodiUri;
    postitoimipaikka?: string;
    email?: string;
    www?: string;
    numero?: string;
};

export type YhteystiedotBase = {
    postiOsoite: string;
    postiOsoitePostiNro: string;
    postiOsoiteToimipaikka: string;
    kayntiOsoite: string;
    kayntiOsoitePostiNro: string;
    kayntiOsoiteToimipaikka: string;
    puhelinnumero: string;
    email: string;
    www: string;
};

export type Yhteystiedot = {
    fi: YhteystiedotBase;
    sv: YhteystiedotBase;
    en: YhteystiedotBase;
    osoitteetOnEri?: boolean;
};

export type Nimi = {
    fi?: string;
    sv?: string;
    en?: string;
};

export type OrganisaationNimetNimi = {
    nimi: Nimi;
    alkuPvm?: string;
};
export type UiOrganisaatioBase = {
    oid: string;
    status: string;
    yritysmuoto?: string;
    nimet: OrganisaationNimetNimi[];
    parentOid: string;
    parentOidPath: string;
    apiYhteystiedot: ApiYhteystiedot[]; // this is needed for combining the values befor update
    currentNimi: Nimi; //  needed for merging and combining orgs
};

export type UiOrganisaatio = UiOrganisaatioBase & Perustiedot & Yhteystiedot;

export type NewUiOrganisaatio = Omit<UiOrganisaatio, 'oid' | 'status' | 'parentOidPath'>;

export type Perustiedot = {
    ytunnus?: string;
    nimi: Nimi;
    organisaatioTyypit: KoodiUri[];
    alkuPvm: LocalDate;
    kotipaikka: KoodistoSelectOption;
    maa: KoodistoSelectOption;
    muutKotipaikat: KoodistoSelectOption[];
    kielet: KoodistoSelectOption[];
};

export type ParentTiedot = {
    organisaatioTyypit: KoodiUri[];
    oid: string;
};

export type NewRyhma = Omit<Ryhma, 'oid'>;

export type Ryhma = {
    oid?: string;
    yritysmuoto?: string; // TODO Tuleeko nämä???
    kuvaus?: string; // TODO Tuleeko nämä???
    kayntiosoite?: Osoite;
    kayttoryhmat: KoodiUri[];
    kieletUris?: KoodiUri[];
    kuvaus2: RyhmanKuvaus;
    lisatiedot?: string[];
    lakkautusPvm?: string;
    muutKotipaikatUris?: string[];
    muutOppilaitosTyyppiUris?: string[];
    nimi: Nimi;
    nimet: OrganisaationNimetNimi[];
    parentOid?: string;
    parentOidPath?: string;
    piilotettu?: boolean;
    postiosoite?: Osoite;
    ryhmatyypit: KoodiUri[];
    toimipistekoodi?: string;
    tyypit: string[];
    version?: number;
    vuosiluokat?: any[];
    yhteystiedot?: Yhteystiedot[];
    yhteystietoArvos?: any[];
    status: string;
};

export type RyhmanKuvaus = {
    'kieli_fi#1'?: string;
    'kieli_sv#1'?: string;
    'kieli_en#1'?: string;
};
export type OrganisaatioSuhde = {
    alkuPvm: string;
    loppuPvm?: string;
    child: OrganisaatioBase;
    parent: OrganisaatioBase;
};

export type YhdistaOrganisaatioon = {
    newParent?: ApiOrganisaatio;
    date: Date;
    merge: boolean;
};
export type SiirraOrganisaatioon = {
    newParent?: ApiOrganisaatio;
    date: Date;
    merge: boolean;
};
export type OrganisaatioHistoria = {
    childSuhteet: OrganisaatioSuhde[];
    parentSuhteet: OrganisaatioSuhde[];
    liitokset: OrganisaatioSuhde[];
    liittymiset: OrganisaatioSuhde[];
};
export interface YhteystietoTyyppi {
    allLisatietokenttas: any;
    oid?: string;
    nimi: Nimi;
    sovellettavatOppilaitostyyppis: string[];
    sovellettavatOrganisaatios: string[];
    version: number;
}

export interface OrganisaatioNimiJaOid {
    oid: string;
    nimi: Nimi;
}

export type SelectOptionType = {
    value: string;
    label: string;
};

export type TranslatedInputBind = {
    localizationKey: string;
    name: string;
    value: string;
    onChange: (e: React.FormEvent<HTMLInputElement>) => void;
    disabled?: boolean;
};

export type FrontProperties = {
    urlVirkailija: string;
};

export interface Option {
    label: string;
    value: string;
}
export type ResolvedRakenne = {
    type: string[];
    moveTargetType: string[];
    mergeTargetType: string[];
    childTypes: string[];
    showYtj: boolean;
};
export type Rakenne = {
    description: string;
    type: string;
    moveTargetType: string | null;
    mergeTargetType: string | null;
    childTypes: string[];
    showYtj: boolean;
};

export type I18n = {
    translate: (key: string) => string;
    translateWithLang: (key: string, language: Language) => string;
    translateNimi: (nimi: Nimi | undefined) => string;
    enrichMessage: (key: string, replacements: { key: string; value: string }[]) => string;
};

export type Koodisto = {
    uri2Arvo: (uri: KoodiUri) => string | number;
    arvo2Uri: (arvo: KoodiArvo) => string;
    uri2Nimi: (uri: KoodiUri) => string;
    arvo2Nimi: (arvo: KoodiArvo) => string;
    nimet: () => string[];
    koodit: () => Koodi[];
    selectOptions: () => KoodistoSelectOption[];
    uri2SelectOption: (uri: KoodiUri) => KoodistoSelectOption;
};

export type KoodistoContextType = {
    kuntaKoodisto: Koodisto;
    kayttoRyhmatKoodisto: Koodisto;
    ryhmaTyypitKoodisto: Koodisto;
    organisaatioTyypitKoodisto: Koodisto;
    ryhmanTilaKoodisto: Koodisto;
    oppilaitoksenOpetuskieletKoodisto: Koodisto;
    postinumerotKoodisto: Koodisto;
    maatJaValtiotKoodisto: Koodisto;
};

export type Opetuskieli = 'suomi' | 'ruotsi' | 'suomi/ruotsi' | 'saame' | 'muu';

export type SupportedKieli = 'fi' | 'sv' | 'en';

export type HistoriaTaulukkoData = { oid: string; nimiHref: JSX.Element; alkuPvm: string; status: string };
