import Axios, { AxiosPromise, AxiosRequestConfig } from 'axios';
import {
    Koodisto,
    OrganisaatioHistoria,
    OrganisaatioNimiJaOid,
    OrganisaationNimetNimi,
    Perustiedot,
    SiirraOrganisaatioon,
    YhdistaOrganisaatioon,
    Yhteystiedot,
} from '../types/types';
import { info, success, warning } from '../components/Notification/Notification';
import {
    ApiOrganisaatio,
    APIOrganisaatioHistoria,
    ApiYhteystiedot,
    NewApiOrganisaatio,
    OrganisaatioBase,
    OrganisaatioLiitos,
    YhteystiedotEmail,
    YhteystiedotOsoite,
    YhteystiedotPhone,
    YhteystiedotWww,
} from '../types/apiTypes';
import useAxios, { RefetchOptions, ResponseValues } from 'axios-hooks';
import { errorHandlingWrapper, useErrorHandlingWrapper } from './errorHandling';
import { ROOT_OID, PUBLIC_API_CONTEXT } from '../contexts/contexts';

type SupportedOsoiteType = 'kaynti' | 'posti';
type SupportedYhteystietoType = 'www' | 'email' | 'numero';

const NAME_WWW = 'www';
const NAME_EMAIL = 'email';
const NAME_PHONE = 'numero';

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
async function readOrganisaatioPath(oids: string[]): Promise<OrganisaatioNimiJaOid[]> {
    if (oids.length === 0) return [];
    const orgTree = await Axios.post(`${baseUrl}findbyoids`, oids);
    const polku = oids.map((oid: string) => ({
        oid,
        nimi: orgTree.data.find((o: ApiOrganisaatio) => o.oid === oid).nimi,
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
}): Promise<ApiOrganisaatio[]> {
    if (searchStr.length < 3) return [];
    const { data } = await Axios.get<{ organisaatiot: ApiOrganisaatio[] }>(`${baseUrl}hierarkia/hae`, {
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
        const response = await Axios.get<ApiOrganisaatio>(`${baseUrl}${oid}?includeImage=true`);
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

const initializeApiOsoite = (kieli: string, osoiteTyyppi: SupportedOsoiteType): YhteystiedotOsoite => ({
    kieli,
    osoiteTyyppi,
    postinumeroUri: '',
    postitoimipaikka: '',
    osoite: '',
    isNew: true,
});

const isApiOsoite = (yhteystieto: ApiYhteystiedot): yhteystieto is YhteystiedotOsoite =>
    yhteystieto.hasOwnProperty('osoiteTyyppi');

function getApiOsoite(
    yhteystiedot: ApiYhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedOsoiteType
): YhteystiedotOsoite {
    const found = yhteystiedot.find(
        (yhteystieto: ApiYhteystiedot) =>
            isApiOsoite(yhteystieto) && yhteystieto.kieli === kieli && yhteystieto.osoiteTyyppi === osoiteTyyppi
    );
    if (found) {
        return found as YhteystiedotOsoite;
    }
    yhteystiedot.push(initializeApiOsoite(kieli, osoiteTyyppi));
    return getApiOsoite(yhteystiedot, kieli, osoiteTyyppi);
}

function getApiYhteystieto(
    yhteystiedot: ApiYhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedYhteystietoType
): ApiYhteystiedot {
    const found = yhteystiedot.find(
        (yhteystieto: ApiYhteystiedot) => yhteystieto.kieli === kieli && yhteystieto.hasOwnProperty(osoiteTyyppi)
    );
    if (found) {
        return found as ApiYhteystiedot;
    }
    yhteystiedot.push({ kieli, [osoiteTyyppi]: '', isNew: true } as ApiYhteystiedot);
    return getApiYhteystieto(yhteystiedot, kieli, osoiteTyyppi);
}

function mapUiOrganisaatioToApiToSave(
    postinumerotKoodisto: Koodisto,
    yhteystiedotFormValues: Yhteystiedot,
    perustiedotFormValues: Perustiedot,
    parentOid: string | undefined
): NewApiOrganisaatio {
    const yhteystiedot = mapUiYhteystiedotToApi(postinumerotKoodisto, [], yhteystiedotFormValues);
    const { kotipaikka, maa, kielet, muutKotipaikat, organisaatioTyypit, alkuPvm, nimi } = perustiedotFormValues;
    const nimet = [
        {
            nimi,
            alkuPvm: new Date().toISOString().split('T')[0],
        },
    ];
    return {
        alkuPvm,
        tyypit: organisaatioTyypit,
        kotipaikkaUri: kotipaikka.value,
        maaUri: maa.value,
        kieletUris: kielet.map((a) => a.value),
        muutKotipaikatUris: muutKotipaikat?.map((a) => a.value) || [],
        yhteystiedot,
        parentOid: parentOid || ROOT_OID,
        nimet,
        nimi,
    };
}

function mapUiOrganisaatioToApiToUpdate(
    postinumerotKoodisto,
    organisaatioBase,
    yhteystiedotFormValues,
    perustiedotFormValues
): ApiOrganisaatio {
    const { oid, parentOid, parentOidPath, status } = organisaatioBase;
    const yhteystiedot = mapUiYhteystiedotToApi(
        postinumerotKoodisto,
        organisaatioBase.apiYhteystiedot,
        yhteystiedotFormValues
    );
    const { kotipaikka, maa, kielet, organisaatioTyypit, muutKotipaikat, alkuPvm } = perustiedotFormValues;
    const today = new Date().toISOString().split('T')[0];
    const nimet = organisaatioBase.nimet || [];
    const uusiNimi = { ...perustiedotFormValues.nimi };
    const sameDayNimiIdx = organisaatioBase.nimet.findIndex((nimi: OrganisaationNimetNimi) => nimi?.alkuPvm === today);
    if (sameDayNimiIdx > -1) {
        nimet[sameDayNimiIdx].nimi = uusiNimi;
    } else {
        nimet.push({ nimi: uusiNimi, alkuPvm: today });
    }
    return {
        alkuPvm,
        oid,
        parentOid,
        parentOidPath,
        status,
        yhteystiedot,
        nimet,
        nimi: uusiNimi,
        tyypit: organisaatioTyypit,
        muutKotipaikatUris: muutKotipaikat?.map((a) => a.value) || [],
        kotipaikkaUri: kotipaikka?.value,
        maaUri: maa?.value,
        kieletUris: kielet?.map((a) => a.value) || [],
    };
}

function mapApiYhteystiedotToUi(
    postinumerotKoodisto: Koodisto,
    yhteystiedot: ApiYhteystiedot[] = [],
    kielet = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1']
): Yhteystiedot {
    return {
        ...kielet.reduce(
            (uiYhteystiedot, kieli) => (
                (uiYhteystiedot[kieli] = {
                    postiOsoite: getApiOsoite(yhteystiedot, kieli, 'posti').osoite,
                    postiOsoitePostiNro: postinumerotKoodisto.uri2Nimi(
                        getApiOsoite(yhteystiedot, kieli, 'posti').postinumeroUri
                    ),
                    postiOsoiteToimipaikka: getApiOsoite(yhteystiedot, kieli, 'posti').postitoimipaikka,
                    kayntiOsoite: getApiOsoite(yhteystiedot, kieli, 'kaynti').osoite,
                    kayntiOsoitePostiNro: postinumerotKoodisto.uri2Nimi(
                        getApiOsoite(yhteystiedot, kieli, 'kaynti').postinumeroUri
                    ),
                    kayntiOsoiteToimipaikka: getApiOsoite(yhteystiedot, kieli, 'kaynti').postitoimipaikka,
                    puhelinnumero: getApiYhteystieto(yhteystiedot, kieli, NAME_PHONE)[NAME_PHONE],
                    email: getApiYhteystieto(yhteystiedot, kieli, NAME_EMAIL)[NAME_EMAIL],
                    www: getApiYhteystieto(yhteystiedot, kieli, NAME_WWW)[NAME_WWW],
                }),
                uiYhteystiedot
            ),
            {} as Yhteystiedot
        ),
        osoitteetOnEri: false,
    };
}

function mapUiYhteystiedotToApi(
    postinumerotKoodisto: Koodisto,
    apiYhteystiedot: ApiYhteystiedot[] = [],
    uiYhteystiedot: Yhteystiedot
): ApiYhteystiedot[] {
    const { osoitteetOnEri, ...rest } = uiYhteystiedot;
    return Object.keys(rest)
        .map((kieli) => {
            const postiosoite = getApiOsoite(apiYhteystiedot, kieli, 'posti');
            postiosoite.osoite = uiYhteystiedot[kieli].postiOsoite;
            postiosoite.postinumeroUri = postinumerotKoodisto.arvo2Uri(uiYhteystiedot[kieli].postiOsoitePostiNro);
            postiosoite.postitoimipaikka = uiYhteystiedot[kieli].postiOsoiteToimipaikka;
            const kayntiosoite = getApiOsoite(apiYhteystiedot, kieli, 'kaynti');
            if (
                uiYhteystiedot.osoitteetOnEri === true &&
                !!uiYhteystiedot[kieli].kayntiOsoite &&
                !!uiYhteystiedot[kieli].kayntiOsoitePostiNro
            ) {
                kayntiosoite.osoite = uiYhteystiedot[kieli].kayntiOsoite;
                kayntiosoite.postinumeroUri = postinumerotKoodisto.arvo2Uri(uiYhteystiedot[kieli].kayntiOsoitePostiNro);
                kayntiosoite.postitoimipaikka = uiYhteystiedot[kieli].kayntiOsoiteToimipaikka;
            } else if (uiYhteystiedot.osoitteetOnEri === false) {
                kayntiosoite.osoite = postiosoite.osoite;
                kayntiosoite.postinumeroUri = postiosoite.postinumeroUri;
                kayntiosoite.postitoimipaikka = postiosoite.postitoimipaikka;
            }
            const puhelinnumero = getApiYhteystieto(apiYhteystiedot, kieli, NAME_PHONE) as YhteystiedotPhone;
            if (!!uiYhteystiedot[kieli].puhelinnumero) {
                puhelinnumero.tyyppi = 'puhelin';
                puhelinnumero[NAME_PHONE] = uiYhteystiedot[kieli].puhelinnumero;
            }
            const email = getApiYhteystieto(apiYhteystiedot, kieli, NAME_EMAIL);
            if (!!uiYhteystiedot[kieli].email) {
                email[NAME_EMAIL] = uiYhteystiedot[kieli].email;
            }
            const www = getApiYhteystieto(apiYhteystiedot, kieli, NAME_WWW);
            if (!!uiYhteystiedot[kieli].www) {
                www[NAME_WWW] = uiYhteystiedot[kieli].www;
            }
            return checkAndMapValuesToYhteystiedot([postiosoite, kayntiosoite, puhelinnumero, email, www]);
        })
        .reduce((a, b) => a.concat(b)) as ApiYhteystiedot[];
}

export const checkAndMapValuesToYhteystiedot = (yhteystiedotObjectsArray: ApiYhteystiedot[]): ApiYhteystiedot[] => {
    return yhteystiedotObjectsArray
        .map((yhteystieto) => {
            const { isNew, ...rest } = yhteystieto;
            if (
                !isNew ||
                (isNew &&
                    (!!(yhteystieto as YhteystiedotOsoite).osoite ||
                        !!(yhteystieto as YhteystiedotPhone)[NAME_PHONE] ||
                        !!(yhteystieto as YhteystiedotEmail)[NAME_EMAIL] ||
                        !!(yhteystieto as YhteystiedotWww)[NAME_WWW]))
            ) {
                return { ...rest };
            }
        })
        .filter(Boolean) as ApiYhteystiedot[];
};

function transformData(data: APIOrganisaatioHistoria): OrganisaatioHistoria {
    function liitosMapper(
        a: OrganisaatioLiitos
    ): { alkuPvm: string; parent: OrganisaatioBase; loppuPvm: string | undefined; child: OrganisaatioBase } {
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
    organisaatiot: ApiOrganisaatio[];
    organisaatiotLoading: boolean;
    organisaatiotError: boolean;
} {
    return useErrorHandlingWrapper(function useHorse() {
        const [{ data, loading, error }]: [
            ResponseValues<{ organisaatiot: ApiOrganisaatio[] }>,
            (
                config?: AxiosRequestConfig,
                options?: RefetchOptions
            ) => AxiosPromise<{ organisaatiot: ApiOrganisaatio[] }>
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
    getApiOsoite,
    getApiYhteystieto,
    mapApiYhteystiedotToUi,
    mapUiYhteystiedotToApi,
    mapUiOrganisaatioToApiToSave,
    mapUiOrganisaatioToApiToUpdate,
    useOrganisaatioHistoria,
    useOrganisaatioHaku,
    createOrganisaatio,
    readOrganisaatio,
    updateOrganisaatio,
    mergeOrganisaatio,
    searchOrganisation,
};