import useAxios, { RefetchOptions, ResponseValues } from 'axios-hooks';
import { OrganisaatioHistoria } from '../types/types';
import { AxiosPromise, AxiosRequestConfig } from 'axios';
import { APIOrganisaatioHistoria } from '../types/apiTypes';

function transformData(data: APIOrganisaatioHistoria): OrganisaatioHistoria {
    return data;
}

export default function useOrganisaatioHistoria(oid) {
    const [{ data, loading, error }]: [
        ResponseValues<OrganisaatioHistoria>,
        (config?: AxiosRequestConfig, options?: RefetchOptions) => AxiosPromise<OrganisaatioHistoria>
    ] = useAxios(`/organisaatio/organisaatio/v4/${oid}/historia`);
    return { historia: transformData(data), historiaLoading: loading, historiaError: error };
}
