import Axios, { AxiosPromise, AxiosRequestConfig } from 'axios';
import {
    NewOrganisaatio,
    Organisaatio,
    OrganisaatioHistoria,
    OrganisaatioNimiJaOid,
    OrganisaatioSuhde,
} from '../types/types';
import { info, success } from '../components/Notification/Notification';
import { APIOrganisaatioHistoria, OrganisaatioLiitos } from '../types/apiTypes';
import useAxios, { RefetchOptions, ResponseValues } from 'axios-hooks';
import { errorHandlingWrapper, useErrorHandlingWrapper } from './errorHandling';

const baseUrl = `/organisaatio/organisaatio/v4/`;

async function createOrganisaatio(organisaatio: NewOrganisaatio) {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.post(`${baseUrl}`, organisaatio);
        success({ message: 'MESSAGE_TALLENNUS_ONNISTUI' });
        return data.organisaatio as Organisaatio;
    });
}

async function updateOrganisaatio(organisaatio: Organisaatio) {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.put(`${baseUrl}${organisaatio.oid}`, organisaatio);
        success({ message: 'MESSAGE_TALLENNUS_ONNISTUI' });
        return data.organisaatio as Organisaatio;
    });
}
async function readOrganisaatioPath(oids: string[]): Promise<OrganisaatioNimiJaOid[]> {
    if (oids.length === 0) return [];
    const orgTree = await Axios.post(`${baseUrl}findbyoids`, oids);
    const polku = oids.map((oid: string) => ({
        oid,
        nimi: orgTree.data.find((o: Organisaatio) => o.oid === oid).nimi,
    }));
    return polku;
}
async function readOrganisaatio(oid: string) {
    return errorHandlingWrapper(async () => {
        const response = await Axios.get<Organisaatio>(`${baseUrl}${oid}?includeImage=true`);
        const organisaatio = response.data;
        const idArr = organisaatio.parentOidPath.split('|').filter((val: string) => val !== '');
        const polku = await readOrganisaatioPath(idArr);
        info({ message: 'MESSAGE_LATAUS_ONNISTUI', timeOut: 200 });
        return { organisaatio: organisaatio, polku: polku };
    });
}

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

function useOrganisaatioHistoria(oid) {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }]: [
            ResponseValues<APIOrganisaatioHistoria>,
            (config?: AxiosRequestConfig, options?: RefetchOptions) => AxiosPromise<APIOrganisaatioHistoria>
        ] = useAxios(`${baseUrl}${oid}/historia`);
        return { historia: data && transformData(data), historiaLoading: loading, historiaError: error };
    });
}

export { useOrganisaatioHistoria, createOrganisaatio, readOrganisaatio, updateOrganisaatio };
