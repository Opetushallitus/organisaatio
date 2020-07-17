export type Language = 'fi' | 'sv' | 'en';
export type LocalDate = string;
export type LocalizableText = Partial<Record<Language, string>>;

// koodisto
export type KoodiUri = string;
export type KoodiArvo = string;
export type Koodi = {
    uri: KoodiUri,
    arvo: KoodiArvo,
    nimi: LocalizableText,
};

// lokalisointi
export type Lokalisointi = Record<Language, Record<string, string>>;

export type KielistettyNimi = {
    nimi: string,
    kieli: Language, // ytj-kieli
    alkuPvm: LocalDate | null
}

export type Osoite = {
    katuosoite: string,
    postinumeroUri: KoodiUri,
    postitoimipaikka: string
}

export type Yhteystiedot = {
    puhelinnumero: string,
    sahkoposti: string,
    postiosoite: Osoite,
    kayntiosoite: Osoite
}
export interface Organisaatio {
    oid?: string,
    ytunnus: string,
    nimi: any,
    nimet: any,
    alkuPvm: LocalDate | null,
    yritysmuoto: string,
    tyypit: KoodiUri[],
    kotipaikkaUri: KoodiUri,
    maaUri: KoodiUri,
    kieletUris: KoodiUri[],
    yhteystiedot: Yhteystiedot
}

export interface Ryhma {
    nimi: any,
    yritysmuoto: string,
    tyypit: string[],
    kayttoryhmat: string[]
    kuvaus: any,
    oid?: string,
    parentOid: string,
    parentOidPath?: string,
    ryhmatyypit: string[],
    status: string,
    version: number,
}

export interface YhteystietoTyyppi {
    allLisatietokenttas: any,
    oid?: string,
    nimi: any,
    sovellettavatOppilaitostyyppis: string[],
    sovellettavatOrganisaatios: string[],
    version: number,
}

export interface OrganisaatioNimiJaOid {
    oid: string,
    nimi: any,
}

// kayttooikeus
export type Kayttaja = {
    etunimi: string,
    sukunimi: string,
    sahkoposti: string,
    asiointikieli: string,
    saateteksti: string,
}

export type VirheKoodi = string;
export type Virheet = Record<string, VirheKoodi>
