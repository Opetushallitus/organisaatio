import { KoodiUri, LocalDate, Nimi, OrganisaatioType } from './types';

//yyyy-MM-DD
export type APIEndpontDate = `${number}${number}${number}${number}-${number}${number}-${number}${number}` | '';

export type APIOrganisaatioLiitos = {
    alkuPvm: APIEndpontDate;
    loppuPvm?: APIEndpontDate;
    kohde: OrganisaatioBase;
    organisaatio: OrganisaatioBase;
};
export type APIOrganisaatioSuhde = {
    alkuPvm: APIEndpontDate;
    loppuPvm?: APIEndpontDate;
    child: OrganisaatioBase;
    parent: OrganisaatioBase;
};

export type APIOrganisaatioHistoria = {
    childSuhteet: APIOrganisaatioSuhde[];
    parentSuhteet: APIOrganisaatioSuhde[];
    liitokset: APIOrganisaatioLiitos[];
    liittymiset: APIOrganisaatioLiitos[];
};

export type ApiOrganisaationNimetNimi = {
    nimi: Nimi;
    alkuPvm: APIEndpontDate;
};

type YhteystiedotBase = {
    id?: string;
    yhteystietoOid?: string;
    kieli: string;
    isNew?: boolean;
};

export type YhteystiedotEmail = YhteystiedotBase & {
    email: string;
};

export type YhteystiedotPhone = YhteystiedotBase & {
    tyyppi: 'puhelin';
    numero: string;
};

export type YhteystiedotWww = YhteystiedotBase & {
    www: string;
};

type OsoiteType = 'posti' | 'kaynti' | 'ulkomainen_posti' | 'ulkomainen_kaynti' | 'muu';

export type YhteystiedotOsoite = YhteystiedotBase & {
    osoiteTyyppi: OsoiteType;
    postinumeroUri: KoodiUri;
    postitoimipaikka: string;
    osoite: string;
};

export type ApiYhteystiedot = YhteystiedotEmail | YhteystiedotPhone | YhteystiedotWww | YhteystiedotOsoite;

export type OrganisaatioBase = {
    oid: string;
    status: string;
    nimi: Nimi;
    lyhytNimi: Nimi;
    parentOid: string;
    parentOidPath: string;
    tarkastusPvm?: number;
};
export type ApiYhteystietoArvo = {
    'YhteystietoArvo.arvoText': string;
    'YhteystietoArvo.kieli': string;
    'YhteystietojenTyyppi.oid': string;
    'YhteystietoElementti.oid': string;
    'YhteystietoElementti.pakollinen': boolean;
    'YhteystietoElementti.kaytossa': boolean;
};
export type ApiVakaTiedot = {
    toimintamuoto: KoodiUri;
    kasvatusopillinenJarjestelma: KoodiUri;
    paikkojenLukumaara: number;
    varhaiskasvatuksenToiminnallinenpainotukset: {
        toiminnallinenpainotus: KoodiUri;
        alkupvm: LocalDate;
        loppupvm?: LocalDate;
    }[];
    varhaiskasvatuksenKielipainotukset: {
        kielipainotus: KoodiUri;
        alkupvm: LocalDate;
        loppupvm?: LocalDate;
    }[];
    varhaiskasvatuksenJarjestamismuodot: KoodiUri[];
};
export type ApiOrganisaatio = OrganisaatioBase & {
    alkuPvm: APIEndpontDate;
    lakkautusPvm?: APIEndpontDate;
    parentOid: string;
    parentOidPath: string;
    yritysmuoto?: string;
    ytunnus?: string;
    tyypit: OrganisaatioType[];
    status: string;
    nimet: ApiOrganisaationNimetNimi[];
    kotipaikkaUri: KoodiUri;
    muutKotipaikatUris?: KoodiUri[];
    maaUri: KoodiUri;
    kieletUris: KoodiUri[];
    yhteystiedot: ApiYhteystiedot[];
    oppilaitosTyyppiUri: KoodiUri;
    oppilaitosKoodi: string;
    muutOppilaitosTyyppiUris: string[];
    vuosiluokat: string[];
    varhaiskasvatuksenToimipaikkaTiedot?: ApiVakaTiedot;
    piilotettu?: boolean;
    yhteystietoArvos?: ApiYhteystietoArvo[];
    subRows?: ApiOrganisaatio[];
    virastoTunnus?: string;
};

export type NewApiOrganisaatio = Omit<ApiOrganisaatio, 'oid' | 'status' | 'parentOidPath'>;
