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

export interface Organisaatio {
    oid?: string;
    ytunnus: string;
    nimi: any; // TODO?
    nimet: any; // TODO?
    alkuPvm: LocalDate | null;
    yritysmuoto: string;
    tyypit: KoodiUri[];
    kotipaikkaUri: KoodiUri;
    muutKotipaikatUris: KoodiUri[];
    maaUri: KoodiUri;
    kieletUris: KoodiUri[];
    yhteystiedot: Yhteystiedot[];
}

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

export interface Ryhma {
    nimi: any;
    yritysmuoto: string;
    tyypit: string[];
    kayttoryhmat: string[];
    kuvaus: any;
    kuvaus2?: any;
    oid?: string;
    parentOid: string;
    parentOidPath?: string;
    ryhmatyypit: string[];
    status: string;
    version: number;
}

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

// kayttooikeus
export type Kayttaja = {
    etunimi: string;
    sukunimi: string;
    sahkoposti: string;
    asiointikieli: string;
    saateteksti: string;
};

export type VirheKoodi = string;
export type SelectOptionType = {
    value: string;
    label: string;
};
