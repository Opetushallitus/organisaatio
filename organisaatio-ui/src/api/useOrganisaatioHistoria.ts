import useAxios, { RefetchOptions, ResponseValues } from 'axios-hooks';
import { OrganisaatioHistoria, OrganisaatioSuhde } from '../types/types';
import { AxiosPromise, AxiosRequestConfig } from 'axios';
import { APIOrganisaatioHistoria, OrganisaatioLiitos } from '../types/apiTypes';

function transformData(data: APIOrganisaatioHistoria): OrganisaatioHistoria {
    function liitosMapper(a: OrganisaatioLiitos): OrganisaatioSuhde {
        return { alkuPvm: a.alkuPvm, loppuPvm: a.loppuPvm, child: a.kohde, parent: a.organisaatio };
    }

    return {
        childSuhteet: data.childSuhteet,
        parentSuhteet: data.parentSuhteet,
        liitokset: data.liitokset.map(liitosMapper),
        liittymiset: data.liittymiset.map(liitosMapper),
    };
}

export default function useOrganisaatioHistoria(oid) {
    const [{ data, loading, error }]: [
        ResponseValues<APIOrganisaatioHistoria>,
        (config?: AxiosRequestConfig, options?: RefetchOptions) => AxiosPromise<APIOrganisaatioHistoria>
    ] = useAxios(`/organisaatio/organisaatio/v4/${oid}/historia`);
    return { historia: data && transformData(data), historiaLoading: loading, historiaError: error };
}
