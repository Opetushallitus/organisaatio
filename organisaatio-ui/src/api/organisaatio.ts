import Axios, { CancelToken } from 'axios';
import {
    LiitaOrganisaatioon,
    OrganisaatioNimiJaOid,
    OrganisaatioPaivittaja,
    UiOrganisaationNimetNimi,
} from '../types/types';
import { success, warning } from '../components/Notification/Notification';
import {
    ApiOrganisaatio,
    APIOrganisaatioHistoria,
    ApiOrganisaationNimetNimi,
    NewApiOrganisaatio,
    OrganisaatioHakuOrganisaatio,
} from '../types/apiTypes';
import useAxios from 'axios-hooks';
import { errorHandlingWrapper, useErrorHandlingWrapper } from './errorHandling';
import { PUBLIC_API_CONTEXT } from '../contexts/constants';
import { formatUiDateStrToApi } from '../tools/mappers';
import { transformData } from './organisaatioMappers';

const baseUrl = `${PUBLIC_API_CONTEXT}/`;

async function createOrganisaatio(organisaatio: NewApiOrganisaatio) {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.post<{ organisaatio: ApiOrganisaatio }>(`${baseUrl}`, organisaatio);
        success({ message: 'MESSAGE_TALLENNUS_ONNISTUI' });
        return data.organisaatio;
    });
}

async function updateOrganisaatio(organisaatio: ApiOrganisaatio) {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.put<{ organisaatio: ApiOrganisaatio }>(
            `${baseUrl}${organisaatio.oid}`,
            organisaatio
        );
        success({ message: 'MESSAGE_TALLENNUS_ONNISTUI' });
        return data.organisaatio;
    });
}
async function deleteOrganisaatio(oid: string) {
    return errorHandlingWrapper(async () => {
        await Axios.delete<{ organisaatio: ApiOrganisaatio }>(`${baseUrl}${oid}`);
        success({ message: 'MESSAGE_POISTO_ONNISTUI' });
    });
}

async function createOrganisaatioNimi(oid: string, { nimi, alkuPvm }: UiOrganisaationNimetNimi) {
    const apiNimi = { nimi, alkuPvm: formatUiDateStrToApi(alkuPvm) };
    return errorHandlingWrapper(async () => {
        const { data: nimiData } = await Axios.post<ApiOrganisaationNimetNimi>(`${baseUrl}${oid}/nimet`, apiNimi);
        success({ message: 'MESSAGE_NIMEN_TALLENNUS_ONNISTUI' });
        return nimiData;
    });
}

async function updateOrganisaatioNimi(
    oid: string,
    currentUiNimi: UiOrganisaationNimetNimi,
    updatedUiNimi: UiOrganisaationNimetNimi
) {
    const requestBody = {
        currentNimi: { ...currentUiNimi, alkuPvm: formatUiDateStrToApi(currentUiNimi.alkuPvm) },
        updatedNimi: { ...updatedUiNimi, alkuPvm: formatUiDateStrToApi(updatedUiNimi.alkuPvm) },
    };
    return errorHandlingWrapper(async () => {
        const { data: nimi } = await Axios.put<ApiOrganisaationNimetNimi>(`${baseUrl}${oid}/nimet`, requestBody);
        success({ message: 'MESSAGE_NIMEN_MUOKKAUS_ONNISTUI' });
        return nimi;
    });
}

async function deleteOrganisaatioNimi(oid: string, { nimi, alkuPvm }: UiOrganisaationNimetNimi) {
    const data = { nimi, alkuPvm: formatUiDateStrToApi(alkuPvm) };
    return errorHandlingWrapper(async () => {
        await Axios.delete(`${baseUrl}${oid}/nimet`, { data });
        success({ message: 'MESSAGE_NIMEN_POISTO_ONNISTUI' });
    });
}

async function readOrganisaatioPath(oids: string[]): Promise<OrganisaatioNimiJaOid[]> {
    if (oids.length === 0) return [];
    const orgTree = await Axios.post(`${baseUrl}findbyoids`, oids);
    return oids.map((oid: string) => ({
        oid,
        nimi: orgTree.data.find((o: ApiOrganisaatio) => o.oid === oid)?.lyhytNimi ?? '???',
    }));
}

function useOrganisaatioPaivittaja(
    oid: string
): {
    data: OrganisaatioPaivittaja;
    loading: boolean;
    error: boolean;
    execute: () => void;
} {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }, execute] = useAxios<OrganisaatioPaivittaja>(
            { url: `${baseUrl}${oid}/paivittaja`, method: 'GET' },
            { manual: true }
        );
        return {
            data,
            loading,
            error,
            execute,
        };
    });
}

async function getJalkelaiset({ oid }: { oid: string }): Promise<OrganisaatioHakuOrganisaatio[]> {
    const { data } = await Axios.get<{ organisaatiot: OrganisaatioHakuOrganisaatio[] }>(`${baseUrl}${oid}/jalkelaiset`);
    return data.organisaatiot;
}
async function searchOrganisation(
    params: {
        searchStr: string;
        aktiiviset: boolean;
        lakkautetut: boolean;
        suunnitellut: boolean;
        organisaatiotyyppi?: string;
        oppilaitostyyppi?: string;
    },
    cancelToken: CancelToken
): Promise<OrganisaatioHakuOrganisaatio[]> {
    const { data } = await Axios.get<{ organisaatiot: OrganisaatioHakuOrganisaatio[] }>(`${baseUrl}hierarkia/hae`, {
        params,
        cancelToken,
    });
    return data.organisaatiot;
}

async function setTarkastusPvm(oid: string) {
    return errorHandlingWrapper(async () => {
        const { data: newTarkistusPvm } = await Axios.put<number>(`${baseUrl}${oid}/tarkasta`);
        success({ message: 'MESSAGE_TARKASTUS_AIKA_TALLENNETTU' });
        if (newTarkistusPvm) return newTarkistusPvm;
        else return null;
    });
}

async function readOrganisaatio(
    oid: string,
    parent?: boolean
): Promise<{ organisaatio: ApiOrganisaatio; polku: OrganisaatioNimiJaOid[] } | undefined> {
    return errorHandlingWrapper(async () => {
        const response = await Axios.get<ApiOrganisaatio>(`${baseUrl}${oid}`);
        const organisaatio = response.data;
        if (parent) {
            return { organisaatio, polku: [] };
        }
        const idArr = organisaatio.parentOidPath.split('|').filter((val: string) => val !== '');
        const polku = await readOrganisaatioPath(idArr);
        return { organisaatio, polku };
    });
}

async function mergeOrganisaatio({
    oid,
    newParent,
    date,
    merge,
}: LiitaOrganisaatioon & {
    oid: string;
}) {
    return errorHandlingWrapper(async () => {
        if (newParent) {
            const response = await Axios.put<never>( //?merge=false&moveDate=2021-09-05
                `${baseUrl}${oid}/organisaatiosuhde/${newParent.oid}?merge=${merge}&moveDate=${formatUiDateStrToApi(
                    date
                )}`
            );
            success({ message: 'MESSAGE_LIITOS_ONNISTUI' });
            return response;
        } else {
            warning({ message: 'MESSAGE_LIITOS_UUSI_VANHEMPI_PUUTTUU' });
            return null;
        }
    });
}

function useOrganisaatioHistoria(oid: string) {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }, execute] = useAxios<APIOrganisaatioHistoria>(`${baseUrl}${oid}/historia`);
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
    oppilaitostyyppi = '',
    organisaatiotyyppi = '',
    suunnitellut = true,
}: {
    aktiiviset?: boolean;
    lakkautetut?: boolean;
    oppilaitostyyppi?: string;
    organisaatiotyyppi?: string;
    suunnitellut?: boolean;
}): {
    organisaatiot: ApiOrganisaatio[];
    organisaatiotLoading: boolean;
    organisaatiotError: boolean;
} {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }] = useAxios<{ organisaatiot: ApiOrganisaatio[] }>({
            url: `${baseUrl}hae`,
            params: {
                aktiiviset,
                lakkautetut,
                oppilaitostyyppi,
                organisaatiotyyppi,
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
    deleteOrganisaatio,
    readOrganisaatio,
    updateOrganisaatio,
    mergeOrganisaatio,
    searchOrganisation,
    useOrganisaatioPaivittaja,
    createOrganisaatioNimi,
    updateOrganisaatioNimi,
    deleteOrganisaatioNimi,
    setTarkastusPvm,
    getJalkelaiset,
};
