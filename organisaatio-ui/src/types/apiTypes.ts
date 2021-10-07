import { Language, LocalDate, OrganisaatioBase, OrganisaatioSuhde, ytjOsoite, ytjYtunnus } from './types';

export type CASMe = {
    uid: string;
    oid: string;
    firstName: string;
    lastName: string;
    groups: string[];
    roles: string;
    lang: Language;
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
export type YtjHaku = {
    ytunnus: string;
    nimi: string;
};
export type YtjOrganisaatio = YtjHaku & {
    aloitusPvm: LocalDate | null;
    yritysmuoto: string;
    yritysmuotoKoodi: string;
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
};
