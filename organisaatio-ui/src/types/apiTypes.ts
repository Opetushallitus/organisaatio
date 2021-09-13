import { Language, OrganisaatioBase, OrganisaatioSuhde } from './types';

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
