export type Language = 'fi' | 'sv' | 'en';
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

type Yhteystiedot = {
    puhelinnumero: string;
    sahkoposti: string;
    postiosoite: {
        katuosoite: string;
        postinumeroUri: string;
        postitoimipaikka: string;
    };
    kayntiosoite: {
        katuosoite: string;
        postinumeroUri: string;
        postitoimipaikka: string;
    };
};

export type Organisation = {
    alkuPvm: string;
    kieletUris: string[];
    kotipaikkaUri: string;
    kunta: boolean;
    maaUri: string;
    oid: string;
    tyypit: string[];
    uudelleenRekisterointi: boolean;
    yhteystiedot: Yhteystiedot;
    yritysmuoto: string;
    ytjNimi: { nimi: string; kieli: string; alkuPvm: string };
    ytunnus: string;
};

export type SelectOption = { value: string; label: string };

export type RekisterointiRequest = {
    yritysmuoto: string;
    kotipaikka: string;
    alkamisaika: string;
    puhelinnumero: string;
    email: string;
    postiosoite: string;
    postinumero: string;
    postitoimipaikka: string;
    copyKayntiosoite: boolean;
    kayntiosoite: string;
    kayntipostinumero: string;
    kayntipostitoimipaikka: string;
    emails: string[];
    etunimi: string;
    sukunimi: string;
    paakayttajaEmail: string;
    asiointikieli: Language;
    info?: string;
};
