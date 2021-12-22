import { ApiOrganisaatio, ApiVakaTiedot, ApiYhteystiedot, OrganisaatioBase } from './apiTypes';
import { Path } from 'react-hook-form';

export type Language = 'fi' | 'sv' | 'en';
export type LocalDate = `${number}${number}${number}${number}-${number}${number}-${number}${number}` | '';
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

export type KoodistoSelectOption = {
    value: KoodiUri;
    arvo: KoodiArvo;
    label: string;
    versio: number;
    disabled?: boolean;
};

export type Nimenmuutostyyppi = 'CREATE' | 'EDIT' | 'DELETE';

// lokalisointi
export type Lokalisointi = Record<Language, Record<string, string>>;

export type Osoite = {
    osoite?: string;
    postinumeroUri?: KoodiUri;
    postitoimipaikka?: string;
    email?: string;
    www?: string;
    numero?: string;
};

export type YhteystiedotBase = {
    postiOsoite: string;
    postiOsoitePostiNro: string;
    postiOsoiteToimipaikka: string;
    kayntiOsoite: string;
    kayntiOsoitePostiNro: string;
    kayntiOsoiteToimipaikka: string;
    puhelinnumero: string;
    email: string;
    www: string;
};

export type Yhteystiedot = {
    fi: YhteystiedotBase;
    sv: YhteystiedotBase;
    en: YhteystiedotBase;
    osoitteetOnEri?: boolean;
};
export type YhteystietoArvot = {
    koskiposti?: { fi?: string; sv?: string; en?: string };
};

export type Nimi = {
    fi?: string;
    sv?: string;
    en?: string;
};

export type NimenmuutosLomake = {
    nimi: Nimi;
    alkuPvm: LocalDate;
    muutostyyppi: Nimenmuutostyyppi;
    oid: string;
    editDisabled?: boolean;
};

export type OrganisaationNimetNimi = {
    nimi: Nimi;
    alkuPvm: LocalDate;
};
export type UiOrganisaatioBase = {
    oid: string;
    status: string;
    yritysmuoto?: string;
    nimet: OrganisaationNimetNimi[];
    parentOid: string;
    parentOidPath: string;
    apiYhteystiedot: ApiYhteystiedot[]; // this is needed for combining the values befor update
    currentNimi: Nimi; //  needed for merging and combining orgs
    varhaiskasvatuksenToimipaikkaTiedot?: ApiVakaTiedot;
};

export type UiOrganisaatio = UiOrganisaatioBase & Perustiedot & Yhteystiedot;

export type NewUiOrganisaatio = Omit<UiOrganisaatio, 'oid' | 'status' | 'parentOidPath'>;

export type VakaPainotus = {
    painotus: KoodistoSelectOption;
    alkupvm: Date;
    loppupvm?: Date;
};
export type VakaToimipaikkaTiedot = {
    toimintamuoto: KoodistoSelectOption;
    kasvatusopillinenJarjestelma: KoodistoSelectOption;
    paikkojenLukumaara: number;
    varhaiskasvatuksenToiminnallinenpainotukset: VakaPainotus[];
    varhaiskasvatuksenKielipainotukset: VakaPainotus[];
    varhaiskasvatuksenJarjestamismuodot: KoodistoSelectOption[];
};
export type Perustiedot = {
    ytunnus?: string;
    nimi: Nimi;
    organisaatioTyypit: KoodiUri[];
    alkuPvm: LocalDate;
    kotipaikka: KoodistoSelectOption;
    maa: KoodistoSelectOption;
    muutKotipaikat: KoodistoSelectOption[];
    kielet: KoodistoSelectOption[];
    oppilaitosTyyppiUri: KoodistoSelectOption;
    oppilaitosKoodi: string;
    muutOppilaitosTyyppiUris: KoodistoSelectOption[];
    vuosiluokat: KoodistoSelectOption[];
    lakkautusPvm?: LocalDate;
    varhaiskasvatuksenToimipaikkaTiedot?: VakaToimipaikkaTiedot;
    piilotettu?: boolean;
};

export type ParentTiedot = {
    organisaatioTyypit: KoodiUri[];
    oid: string;
};

export type NewRyhma = Omit<Ryhma, 'oid'>;

export type Ryhma = {
    oid?: string;
    yritysmuoto?: string; // TODO Tuleeko nämä???
    kuvaus?: string; // TODO Tuleeko nämä???
    kayntiosoite?: Osoite;
    kayttoryhmat: KoodiUri[];
    kieletUris?: KoodiUri[];
    kuvaus2: RyhmanKuvaus;
    lisatiedot?: string[];
    lakkautusPvm?: string;
    muutKotipaikatUris?: string[];
    muutOppilaitosTyyppiUris?: string[];
    nimi: Nimi;
    nimet: OrganisaationNimetNimi[];
    parentOid?: string;
    parentOidPath?: string;
    piilotettu?: boolean;
    postiosoite?: Osoite;
    ryhmatyypit: KoodiUri[];
    toimipistekoodi?: string;
    tyypit: string[];
    version?: number;
    yhteystiedot?: Yhteystiedot[];
    status: string;
};

export type RyhmanKuvaus = {
    'kieli_fi#1'?: string;
    'kieli_sv#1'?: string;
    'kieli_en#1'?: string;
};
export type OrganisaatioSuhde = {
    alkuPvm: string;
    loppuPvm?: string;
    child: OrganisaatioBase;
    parent: OrganisaatioBase;
};

export type LiitaOrganisaatioon = {
    newParent?: ApiOrganisaatio;
    date: Date;
    merge: boolean;
};

export type OrganisaatioHistoria = {
    childSuhteet: OrganisaatioSuhde[];
    parentSuhteet: OrganisaatioSuhde[];
    liitokset: OrganisaatioSuhde[];
    liittymiset: OrganisaatioSuhde[];
};

export interface OrganisaatioNimiJaOid {
    oid: string;
    nimi: Nimi;
}
export type OrganisaatioPaivittaja = {
    paivitysPvm?: Date;
    etuNimet?: string;
    sukuNimi?: string;
};

export type SelectOptionType = {
    value: string;
    label: string;
};

export type FrontProperties = {
    urlVirkailija: string;
};

export interface Option {
    label: string;
    value: string;
}

export type DynamicField = {
    name: Path<Perustiedot>;
    label: string;
    koodisto: 'vuosiluokatKoodisto';
    type: 'INPUT' | 'SELECT' | 'MULTI_SELECT' | 'LINK';
    when: [{ field: Path<Perustiedot>; is: string }];
    value?: string;
};

type OrganisaatioChildType = {
    type: string;
    disabled?: boolean;
};
export type ResolvedRakenne = {
    type: string[];
    moveTargetType: string[];
    mergeTargetType: string[];
    childTypes: OrganisaatioChildType[];
    showYtj: boolean;
    dynamicFields: DynamicField[];
};
export type Rakenne = {
    description: string;
    type: string;
    moveTargetType: string | null;
    mergeTargetType: string | null;
    childTypes: OrganisaatioChildType[];
    showYtj: boolean;
    dynamicFields: DynamicField[];
};

export type I18n = {
    translate: (key: string) => string;
    translateWithLang: (key: string, language: Language) => string;
    translateNimi: (nimi: Nimi | undefined) => string;
    enrichMessage: (key: string, replacements: { key: string; value: string }[]) => string;
};

export type Koodisto = {
    uri2Arvo: (uri: KoodiUri) => string | number;
    arvo2Uri: (arvo: KoodiArvo) => string;
    uri2Nimi: (uri: KoodiUri) => string;
    arvo2Nimi: (arvo: KoodiArvo) => string;
    koodit: () => Koodi[];
    selectOptions: () => KoodistoSelectOption[];
    uri2SelectOption: (uri: KoodiUri, disabled?: boolean) => KoodistoSelectOption;
};

export type KoodistoContextType = {
    kuntaKoodisto: Koodisto;
    kayttoRyhmatKoodisto: Koodisto;
    ryhmaTyypitKoodisto: Koodisto;
    organisaatioTyypitKoodisto: Koodisto;
    ryhmanTilaKoodisto: Koodisto;
    oppilaitoksenOpetuskieletKoodisto: Koodisto;
    postinumerotKoodisto: Koodisto;
    maatJaValtiotKoodisto: Koodisto;
    vuosiluokatKoodisto: Koodisto;
    oppilaitostyyppiKoodisto: Koodisto;
    vardatoimintamuotoKoodisto: Koodisto;
    vardakasvatusopillinenjarjestelmaKoodisto: Koodisto;
    vardatoiminnallinenpainotusKoodisto: Koodisto;
    vardajarjestamismuotoKoodisto: Koodisto;
    kielikoodisto: Koodisto;
};

export type Opetuskieli = 'suomi' | 'ruotsi' | 'suomi/ruotsi' | 'saame' | 'muu';

export type SupportedKieli = 'fi' | 'sv' | 'en';

export type HistoriaTaulukkoData = { oid: string; nimiHref: JSX.Element; alkuPvm: string; status: string };

export type SearchFilters = {
    filters: Filters;
    setFilters: (filters: Filters) => void;
};
export type Filters = {
    searchString: string;
    naytaPassivoidut: boolean;
    isOPHVirkailija: boolean;
    omatOrganisaatiotSelected: boolean;
};
