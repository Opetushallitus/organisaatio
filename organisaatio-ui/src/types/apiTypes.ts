import { KoodiUri, Language, LocalDate, Nimi, OrganisaationNimetNimi, OrganisaatioSuhde } from './types';

export type ConfigurableButton =
    | 'LOMAKE_YHDISTA_ORGANISAATIO'
    | 'LOMAKE_SIIRRA_ORGANISAATIO'
    | 'LOMAKE_LISAA_UUSI_TOIMIJA'
    | 'TAULUKKO_LISAA_UUSI_TOIMIJA'
    | 'BUTTON_TALLENNA';
export type CASMe = {
    uid: string;
    oid: string;
    firstName: string;
    lastName: string;
    groups: string[];
    roles: string[];
    lang: Language;
    canHaveButton: (button: ConfigurableButton) => boolean;
};

export type OrganisaatioLiitos = {
    alkuPvm: string;
    loppuPvm?: string;
    kohde: OrganisaatioBase;
    organisaatio: OrganisaatioBase;
};

export type APIOrganisaatioHistoria = {
    childSuhteet: OrganisaatioSuhde[];
    parentSuhteet: OrganisaatioSuhde[];
    liitokset: OrganisaatioLiitos[];
    liittymiset: OrganisaatioLiitos[];
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
    parentOid: string;
    parentOidPath: string;
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
    alkuPvm: LocalDate;
    lakkautusPvm?: LocalDate;
    parentOid: string;
    parentOidPath: string;
    yritysmuoto?: string;
    ytunnus?: string;
    tyypit: KoodiUri[];
    status: string;
    nimet: OrganisaationNimetNimi[];
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
};

export type NewApiOrganisaatio = Omit<ApiOrganisaatio, 'oid' | 'status' | 'parentOidPath'>;
