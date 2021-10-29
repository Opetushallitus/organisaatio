import Axios, { AxiosPromise, AxiosRequestConfig } from 'axios';
import {
    NewOrganisaatio,
    Organisaatio,
    OrganisaatioHistoria,
    OrganisaatioNimiJaOid,
    OrganisaatioSuhde,
    SiirraOrganisaatioon,
    YhdistaOrganisaatioon,
} from '../types/types';
import { info, success, warning } from '../components/Notification/Notification';
import { APIOrganisaatioHistoria, OrganisaatioLiitos } from '../types/apiTypes';
import useAxios, { RefetchOptions, ResponseValues } from 'axios-hooks';
import { errorHandlingWrapper, useErrorHandlingWrapper } from './errorHandling';
import { PUBLIC_API_CONTEXT } from '../contexts/contexts';

const baseUrl = `${PUBLIC_API_CONTEXT}/`;

async function createOrganisaatio(organisaatio: NewOrganisaatio) {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.post<{ organisaatio: Organisaatio }>(`${baseUrl}`, organisaatio);
        success({ message: 'MESSAGE_TALLENNUS_ONNISTUI' });
        return data.organisaatio;
    });
}

async function updateOrganisaatio(organisaatio: Organisaatio) {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.put<{ organisaatio: Organisaatio }>(`${baseUrl}${organisaatio.oid}`, organisaatio);
        success({ message: 'MESSAGE_TALLENNUS_ONNISTUI' });
        return data.organisaatio;
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
async function searchOrganisation({
    searchStr,
    aktiiviset = true,
    lakkautetut = false,
    suunnitellut = true,
}: {
    searchStr: string;
    aktiiviset?: boolean;
    lakkautetut?: boolean;
    suunnitellut?: boolean;
}): Promise<Organisaatio[]> {
    if (searchStr.length < 3) return [];
    const { data } = await Axios.get<{ organisaatiot: Organisaatio[] }>(`${baseUrl}hierarkia/hae`, {
        params: {
            aktiiviset,
            lakkautetut,
            searchStr,
            suunnitellut,
        },
    });

    return data.organisaatiot;
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

async function mergeOrganisaatio({
    oid,
    newParent,
    date,
    merge,
}: (YhdistaOrganisaatioon | SiirraOrganisaatioon) & {
    oid: string;
}) {
    return errorHandlingWrapper(async () => {
        if (newParent) {
            const response = await Axios.put<any>( //?merge=false&moveDate=2021-09-05
                `${baseUrl}${oid}/organisaatiosuhde/${newParent.oid}?merge=${merge}&moveDate=${date.toISOString()}`
            );
            info({ message: 'MESSAGE_LIITOS_ONNISTUI', timeOut: 200 });
            return response;
        } else {
            warning({ message: 'MESSAGE_LIITOS_UUSI_VANHEMPI_PUUTTUU' });
        }
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

function useOrganisaatioHistoria(oid: string) {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }, execute]: [
            ResponseValues<APIOrganisaatioHistoria>,
            (config?: AxiosRequestConfig, options?: RefetchOptions) => AxiosPromise<APIOrganisaatioHistoria>
        ] = useAxios(`${baseUrl}${oid}/historia`);
        return {
            historia: data && transformData(data),
            historiaLoading: loading,
            historiaError: error,
            executeHistoria: execute,
        };
    });
}

function useOrganisaatioHaku({
    aktiiviset = true,
    lakkautetut = false,
    oppilaitosTyyppi = '',
    organisaatioTyyppi = '',
    suunnitellut = true,
}: {
    aktiiviset?: boolean;
    lakkautetut?: boolean;
    oppilaitosTyyppi?: string;
    organisaatioTyyppi?: string;
    suunnitellut?: boolean;
}): {
    organisaatiot: Organisaatio[];
    organisaatiotLoading: boolean;
    organisaatiotError: boolean;
} {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }]: [
            ResponseValues<{ organisaatiot: Organisaatio[] }>,
            (config?: AxiosRequestConfig, options?: RefetchOptions) => AxiosPromise<{ organisaatiot: Organisaatio[] }>
        ] = useAxios({
            url: `${baseUrl}hae`,
            params: {
                aktiiviset,
                lakkautetut,
                oppilaitosTyyppi,
                organisaatioTyyppi,
                suunnitellut,
            },
        });
        return {
            organisaatiot: data && data.organisaatiot.map((o) => o),
            organisaatiotLoading: loading,
            organisaatiotError: error,
        };
    });
}

export {
    useOrganisaatioHistoria,
    useOrganisaatioHaku,
    createOrganisaatio,
    readOrganisaatio,
    updateOrganisaatio,
    mergeOrganisaatio,
    searchOrganisation,
};
