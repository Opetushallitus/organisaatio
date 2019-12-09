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
export type Organisaatio = {
    oid?: string,
    ytunnus: string,
    ytjNimi: KielistettyNimi, // YTJ-kielen mukainen nimi
    alkuPvm: LocalDate | null,
    yritysmuoto: string,
    tyypit: KoodiUri[],
    kotipaikkaUri: KoodiUri,
    maaUri: KoodiUri,
    kieletUris: KoodiUri[],
    yhteystiedot: Yhteystiedot
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
    kayttaja: Kayttaja
}

export type Virheet = Record<string, string>
