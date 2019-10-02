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

// organisaatio
export type Yhteystieto = Record<string, string>;
export type OrganisaatioNimi = {
    alkuPvm: LocalDate | null,
    nimi: LocalizableText,
}
export type Organisaatio = {
    oid?: string,
    ytunnus: string,
    nimi: LocalizableText,
    alkuPvm: LocalDate | null,
    nimet: OrganisaatioNimi[],
    yritysmuoto: string,
    kieletUris: KoodiUri[],
    tyypit: KoodiUri[],
    kotipaikkaUri: KoodiUri,
    maaUri: KoodiUri,
    yhteystiedot: Yhteystieto[],
    ytjkieli: KoodiUri,
}

// kayttooikeus
export type Kayttaja = {
    etunimi: string,
    sukunimi: string,
    sahkoposti: string,
    asiointikieli: string,
    saateteksti: string,
}

// varda-rekisterointi
export type Rekisterointi = {
    organisaatio: Organisaatio,
    sahkopostit: string[],
    toimintamuoto: string,
    kayttaja: Kayttaja
}
