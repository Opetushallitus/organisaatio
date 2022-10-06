import { Rekisterointihakemus } from "../virkailija/rekisterointihakemus";

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

export type RegistrationType = string

export type Permission = {
    hasCreatePermission: boolean
    registrationTypes: RegistrationType[]
}

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
export function tyhjaOrganisaatio(): Organisaatio {
    return {
        ytunnus: '',
        ytjNimi: {
            nimi: '',
            alkuPvm: null,
            kieli: 'fi'
        },
        alkuPvm: null,
        yritysmuoto: '',
        tyypit: [],
        kotipaikkaUri: '',
        maaUri: 'maatjavaltiot1_fin',
        kieletUris: [],
        yhteystiedot: {
            kayntiosoite: {
                katuosoite: '',
                postinumeroUri: '',
                postitoimipaikka: ''
            },
            postiosoite: {
                katuosoite: '',
                postinumeroUri: '',
                postitoimipaikka: ''
            },
            sahkoposti: '',
            puhelinnumero: ''
        }
    }
};

// kayttooikeus
export type Kayttaja = {
    etunimi: string,
    sukunimi: string,
    sahkoposti: string,
    asiointikieli: string,
    saateteksti: string,
}

export type Rekisterointityyppi = 'varda' | 'jotpa'

export type Rekisterointi = {
    toimintamuoto?: string,
    kunnat: string[]
    id: number
    tyyppi: Rekisterointityyppi
    organisaatio: Organisaatio,
    sahkopostit: string[],
    kayttaja: Kayttaja
}

export type VirheKoodi = string;
export type Virheet = Record<string, VirheKoodi>

export type ApprovalCallback = (r: Rekisterointihakemus[], hyvaksytty: boolean, perustelu?: string) => void
