import { ApiYhteystiedot } from './apiTypes';

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

export type ytjOsoite = {
    katu: string;
    postinumero: string;
    toimipaikka: string;
    maa: string;
    kieli: boolean;
};

export type ytjYtunnus = {
    status?: string;
    alkupvm: LocalDate;
    loppupvm: LocalDate;
    yritysLopetettu: boolean;
    ytunnus: string;
};

type YhteystiedotBase = {
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
    'kieli_fi#1': YhteystiedotBase;
    'kieli_sv#1': YhteystiedotBase;
    'kieli_en#1': YhteystiedotBase;
    osoitteetOnEri?: boolean;
};

export type Perustiedot = {
    ytunnus?: string;
    nimi: Nimi;
    tyypit: KoodiUri[];
    alkuPvm: LocalDate;
    kotipaikkaUri: KoodistoSelectOption;
    maaUri: KoodistoSelectOption;
    muutKotipaikatUris: KoodistoSelectOption[];
    kieletUris: KoodistoSelectOption[];
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

export type OrganisaatioBase = {
    oid: string;
    nimi: Nimi;
    status: string;
};
export type Organisaatio = OrganisaatioBase & {
    parentOid: string;
    parentOidPath: string;
    ytunnus?: string;
    nimet: OrganisaationNimetNimi[];
    alkuPvm?: LocalDate;
    yritysmuoto?: string;
    tyypit: KoodiUri[];
    status: string;
    kotipaikkaUri: KoodiUri;
    muutKotipaikatUris?: KoodiUri[];
    maaUri: KoodiUri;
    kieletUris: KoodiUri[];
    yhteystiedot?: ApiYhteystiedot[];
};
export type NewOrganisaatio = Omit<Organisaatio, 'oid' | 'status' | 'parentOidPath'>;

export type NewRyhma = Omit<Ryhma, 'oid'>;

export type Ryhma = Omit<OrganisaatioBase, 'oid'> & {
    oid?: string;
    yritysmuoto?: string; // TODO Tuleeko nämä???
    kuvaus?: string; // TODO Tuleeko nämä???
    kayntiosoite?: any;
    kayttoryhmat: string[];
    kieletUris?: any[];
    kuvaus2: any;
    lisatiedot?: string[];
    lakkautusPvm?: string;
    muutKotipaikatUris?: string[];
    muutOppilaitosTyyppiUris?: string[];
    nimet?: any[];
    parentOid?: string;
    parentOidPath?: string;
    piilotettu?: boolean;
    postiosoite?: any;
    ryhmatyypit: string[];
    toimipistekoodi?: string;
    tyypit: string[];
    version?: number;
    vuosiluokat?: any[];
    yhteystiedot?: Yhteystiedot[];
    yhteystietoArvos?: any[];
};
export type OrganisaatioSuhde = {
    alkuPvm: string;
    loppuPvm?: string;
    child: OrganisaatioBase;
    parent: OrganisaatioBase;
};

export type YhdistaOrganisaatioon = {
    newParent?: Organisaatio;
    date: Date;
    merge: boolean;
};
export type SiirraOrganisaatioon = {
    newParent?: Organisaatio;
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
    nimi: any;
    sovellettavatOppilaitostyyppis: string[];
    sovellettavatOrganisaatios: string[];
    version: number;
}

export interface OrganisaatioNimiJaOid {
    oid: string;
    nimi: any;
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
