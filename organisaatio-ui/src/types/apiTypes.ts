import { OrganisaatioLiitos, OrganisaatioSuhde } from './types';

export type APIOrganisaatioHistoria = {
    childSuhteet: OrganisaatioSuhde[];
    parentSuhteet: OrganisaatioSuhde[];
    liitokset: OrganisaatioLiitos[];
    liittymiset: OrganisaatioLiitos[];
};
