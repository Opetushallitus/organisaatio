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

type LocalizationKey = string;
type LocalizationValue = string;
export type LokalisointiRivi = {
    key: LocalizationKey;
    value: LocalizationValue;
    locale: Language;
};

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

export type Organization = {
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
