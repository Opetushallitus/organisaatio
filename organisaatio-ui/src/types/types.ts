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

// lokalisointi
export type Lokalisointi = Record<Language, Record<string, string>>;

export type KielistettyNimi = {
    nimi: string;
    kieli: Language; // ytj-kieli
    alkuPvm: LocalDate | null;
};

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
    id?: string;
    yhteystietoOid?: string;
    kieli: string;
};

type YhteystiedotEmail = YhteystiedotBase & {
    email: string;
};

type YhteystiedotPhone = YhteystiedotBase & {
    tyyppi: 'puhelin';
    numero: string;
};

type YhteystiedotWww = YhteystiedotBase & {
    www: string;
};

export type OsoiteType = 'posti' | 'kaynti' | 'ulkomainern_posti' | 'ulkomainen_kaynti' | 'muu';

export type YhteystiedotOsoite = YhteystiedotBase & {
    osoiteTyyppi: OsoiteType;
    postinumeroUri: KoodiUri;
    postitoimipaikka: string;
    osoite: string;
};

export type Yhteystiedot = YhteystiedotEmail | YhteystiedotPhone | YhteystiedotWww | YhteystiedotOsoite;

export type Nimi = {
    fi?: string;
    sv?: string;
    en?: string;
};
export type OrganisaatioBase = {
    oid: string;
    nimi: Nimi;
    status: string;
};
export type Organisaatio = OrganisaatioBase & {
    parentOid: string;
    parentOidPath: string;
    ytunnus: string;
    nimet: Nimi[];
    alkuPvm: LocalDate | null;
    yritysmuoto?: string;
    tyypit?: KoodiUri[];
    kotipaikkaUri: KoodiUri;
    muutKotipaikatUris?: KoodiUri[];
    maaUri?: KoodiUri;
    kieletUris: KoodiUri[];
    yhteystiedot?: Yhteystiedot[];
};
export type NewOrganisaatio = Omit<Organisaatio, 'oid'>;

export type NewRyhma = Omit<Ryhma, 'oid'>;

export interface YtjOrganisaatio {
    ytunnus: string;
    nimi: string;
    aloitusPvm: LocalDate | null;
    yritysmuoto: string;
    yritysmuotoKoodi: string;
    yritystunnusHistoria: any; // TODO?
    kayntiOsoite: ytjOsoite;
    kotiPaikka: string;
    kotiPaikkaKoodi?: string;
    postiOsoite: ytjOsoite;
    puhelin: string;
    toimiala: string;
    toimialaKoodi?: string;
    versio?: boolean;
    yrityksenKieli?: string;
    yritysTunnus: ytjYtunnus;
}

export type Ryhma = Omit<OrganisaatioBase, 'oid'> & {
    oid?: string;
    yritysmuoto?: string; // TODO Tuleeko nämä???
    kuvaus?: any; // TODO Tuleeko nämä???
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
    newParent?: string;
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

export interface iOption {
    label: string;
    value: string;
}
