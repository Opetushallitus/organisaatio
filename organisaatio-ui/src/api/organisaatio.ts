import Axios, { CancelToken } from 'axios';
import {
    Koodisto,
    LiitaOrganisaatioon,
    OrganisaatioHistoria,
    OrganisaatioNimiJaOid,
    OrganisaatioPaivittaja,
    OrganisaatioSuhde,
    Perustiedot,
    UiOrganisaatioBase,
    UiOrganisaationNimetNimi,
    Yhteystiedot,
    YhteystiedotBase,
    YhteystietoArvot,
} from '../types/types';
import { success, warning } from '../components/Notification/Notification';
import {
    ApiOrganisaatio,
    APIOrganisaatioHistoria,
    APIOrganisaatioLiitos,
    ApiOrganisaationNimetNimi,
    APIOrganisaatioSuhde,
    ApiYhteystiedot,
    ApiYhteystietoArvo,
    NewApiOrganisaatio,
    OrganisaatioHakuOrganisaatio,
    YhteystiedotEmail,
    YhteystiedotOsoite,
    YhteystiedotPhone,
    YhteystiedotWww,
} from '../types/apiTypes';
import useAxios from 'axios-hooks';
import { errorHandlingWrapper, useErrorHandlingWrapper } from './errorHandling';
import { KOSKIPOSTI_BASE, KOSKIPOSTI_TYYPI_OID, PUBLIC_API_CONTEXT, ROOT_OID } from '../contexts/constants';
import { UnpackNestedValue } from 'react-hook-form';
import { formatUiDateStrToApi, getUiDateStr } from '../tools/mappers';

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
    Object.prototype.hasOwnProperty.call(yhteystieto, 'osoiteTyyppi');

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
        (yhteystieto: ApiYhteystiedot) =>
            yhteystieto.kieli === kieli && Object.prototype.hasOwnProperty.call(yhteystieto, osoiteTyyppi)
    );
    if (found) {
        return found;
    }
    yhteystiedot.push({ kieli, [osoiteTyyppi]: '', isNew: true } as ApiYhteystiedot);
    return getApiYhteystieto(yhteystiedot, kieli, osoiteTyyppi);
}

function mapUiOrganisaatioToApiToSave(
    postinumerotKoodisto: Koodisto,
    yhteystiedotFormValues: Yhteystiedot,
    perustiedotFormValues: Perustiedot,
    parentOid?: string
): NewApiOrganisaatio {
    const yhteystiedot = mapUiYhteystiedotToApi({
        postinumerotKoodisto: postinumerotKoodisto,
        uiYhteystiedot: yhteystiedotFormValues,
    });
    const {
        kotipaikka,
        maa,
        kielet,
        muutKotipaikat,
        organisaatioTyypit,
        alkuPvm,
        nimi = {},
        ytunnus,
        oppilaitosTyyppiUri,
        oppilaitosKoodi,
        muutOppilaitosTyyppiUris,
        vuosiluokat,
        virastoTunnus,
    } = perustiedotFormValues;
    const apiAlkuPvm = formatUiDateStrToApi(alkuPvm);
    const nimet = [
        {
            nimi,
            alkuPvm: apiAlkuPvm,
            version: 0,
        },
    ];
    return {
        ytunnus,
        alkuPvm: apiAlkuPvm,
        lakkautusPvm: '',
        tyypit: organisaatioTyypit,
        kotipaikkaUri: kotipaikka.value,
        maaUri: maa.value,
        kieletUris: kielet.map((a) => `${a.value}#${a.versio}`),
        muutKotipaikatUris: muutKotipaikat?.map((a) => `${a.value}#${a.versio}`) || [],
        yhteystiedot,
        parentOid: parentOid || ROOT_OID,
        nimet,
        nimi: nimi,
        lyhytNimi: nimi,
        oppilaitosTyyppiUri: oppilaitosTyyppiUri && `${oppilaitosTyyppiUri.value}#${oppilaitosTyyppiUri.versio}`,
        oppilaitosKoodi,
        muutOppilaitosTyyppiUris: muutOppilaitosTyyppiUris?.map((a) => `${a.value}#${a.versio}`),
        vuosiluokat: vuosiluokat?.map((a) => `${a.value}#${a.versio}`),
        virastoTunnus,
    };
}

function mapArvot(yhteystietoArvoFormValuet: YhteystietoArvot, field: keyof YhteystietoArvot, base) {
    const arvot = [] as ApiYhteystietoArvo[];
    if (yhteystietoArvoFormValuet[field]?.fi) {
        arvot.push({
            ...base,
            'YhteystietoArvo.arvoText': yhteystietoArvoFormValuet[field]?.fi,
            'YhteystietoArvo.kieli': 'kieli_fi#1',
        });
    }
    if (yhteystietoArvoFormValuet[field]?.sv) {
        arvot.push({
            ...base,
            'YhteystietoArvo.arvoText': yhteystietoArvoFormValuet[field]?.sv,
            'YhteystietoArvo.kieli': 'kieli_sv#1',
        });
    }
    if (yhteystietoArvoFormValuet[field]?.en) {
        arvot.push({
            ...base,
            'YhteystietoArvo.arvoText': yhteystietoArvoFormValuet[field]?.en,
            'YhteystietoArvo.kieli': 'kieli_en#1',
        });
    }
    return arvot;
}

function mapUIYhteystietoArvotToApi(
    yhteystietoArvoFormValuet: YhteystietoArvot,
    originalOrganisaatioArvot: ApiYhteystietoArvo[]
): ApiYhteystietoArvo[] {
    const arvot = [...mapArvot(yhteystietoArvoFormValuet, 'koskiposti', KOSKIPOSTI_BASE)];
    originalOrganisaatioArvot.forEach((a) => {
        const found = arvot.find((b) => {
            return (
                a['YhteystietoElementti.oid'] === b['YhteystietoElementti.oid'] &&
                a['YhteystietojenTyyppi.oid'] === b['YhteystietojenTyyppi.oid'] &&
                a['YhteystietoArvo.kieli'] === b['YhteystietoArvo.kieli']
            );
        });
        if (!found) arvot.push(a);
    });
    return arvot;
}

function mapUiOrganisaatioToApiToUpdate(
    originalOrganisaatio: ApiOrganisaatio,
    postinumerotKoodisto: Koodisto,
    organisaatioBase: UiOrganisaatioBase,
    yhteystiedotFormValues: UnpackNestedValue<Yhteystiedot>,
    perustiedotFormValues: UnpackNestedValue<Perustiedot>,
    yhteystietoArvoFormValuet: UnpackNestedValue<YhteystietoArvot>
): ApiOrganisaatio {
    const yhteystiedot = mapUiYhteystiedotToApi({
        postinumerotKoodisto,
        apiYhteystiedot: organisaatioBase.apiYhteystiedot,
        uiYhteystiedot: yhteystiedotFormValues,
    });
    const yhteystietoArvos = mapUIYhteystietoArvotToApi(
        yhteystietoArvoFormValuet,
        originalOrganisaatio.yhteystietoArvos || []
    );
    const {
        kotipaikka,
        maa,
        kielet,
        organisaatioTyypit,
        muutKotipaikat,
        alkuPvm,
        oppilaitosTyyppiUri,
        oppilaitosKoodi,
        muutOppilaitosTyyppiUris,
        vuosiluokat,
        lakkautusPvm,
        ytunnus,
        piilotettu,
        virastoTunnus,
    } = perustiedotFormValues;
    const apiAlkuPvm = formatUiDateStrToApi(alkuPvm);
    const apiLakkautusPvm = lakkautusPvm ? formatUiDateStrToApi(lakkautusPvm) : '';
    const { currentNimi, nimet } = organisaatioBase;
    return {
        ...originalOrganisaatio,
        lakkautusPvm: apiLakkautusPvm,
        alkuPvm: apiAlkuPvm,
        yhteystiedot,
        ytunnus,
        piilotettu,
        nimi: currentNimi.nimi,
        lyhytNimi: currentNimi.nimi,
        nimet: nimet.map((a) => ({
            nimi: a.nimi,
            alkuPvm: formatUiDateStrToApi(a.alkuPvm),
            version: a.version,
        })),
        tyypit: organisaatioTyypit,
        muutKotipaikatUris: muutKotipaikat.map((a) => `${a.value}#${a.versio}`),
        kotipaikkaUri: kotipaikka.value,
        maaUri: maa.value,
        kieletUris: kielet.map((a) => `${a.value}#${a.versio}`),
        oppilaitosTyyppiUri:
            (!!oppilaitosTyyppiUri?.arvo && `${oppilaitosTyyppiUri.value}#${oppilaitosTyyppiUri.versio}`) || undefined,
        oppilaitosKoodi,
        muutOppilaitosTyyppiUris: muutOppilaitosTyyppiUris?.map((a) => `${a.value}#${a.versio}`),
        vuosiluokat: vuosiluokat?.map((a) => `${a.value}#${a.versio}`),
        yhteystietoArvos,
        virastoTunnus,
    };
}

function kayntiOnEri(yhteysTieto: YhteystiedotBase): boolean {
    return (
        yhteysTieto?.postiOsoite !== yhteysTieto?.kayntiOsoite ||
        yhteysTieto?.postiOsoitePostiNro !== yhteysTieto?.kayntiOsoitePostiNro
    );
}

function mapApiYhteystiedotToUi(
    postinumerotKoodisto: Koodisto,
    yhteystiedot: ApiYhteystiedot[] = [],
    kielet = ['fi', 'sv', 'en']
): Yhteystiedot {
    const yhteysTiedot = kielet.reduce((uiYhteystiedot, kieli) => {
        const apiKieli = `kieli_${kieli}#1`;
        return (
            (uiYhteystiedot[kieli] = {
                postiOsoite: getApiOsoite(yhteystiedot, apiKieli, 'posti').osoite,
                postiOsoitePostiNro: postinumerotKoodisto.uri2Arvo(
                    getApiOsoite(yhteystiedot, apiKieli, 'posti').postinumeroUri
                ),
                postiOsoiteToimipaikka: getApiOsoite(yhteystiedot, apiKieli, 'posti').postitoimipaikka,
                kayntiOsoite: getApiOsoite(yhteystiedot, apiKieli, 'kaynti').osoite,
                kayntiOsoitePostiNro: postinumerotKoodisto.uri2Arvo(
                    getApiOsoite(yhteystiedot, apiKieli, 'kaynti').postinumeroUri
                ),
                kayntiOsoiteToimipaikka: getApiOsoite(yhteystiedot, apiKieli, 'kaynti').postitoimipaikka,
                puhelinnumero: getApiYhteystieto(yhteystiedot, apiKieli, NAME_PHONE)[NAME_PHONE],
                email: getApiYhteystieto(yhteystiedot, apiKieli, NAME_EMAIL)[NAME_EMAIL],
                www: getApiYhteystieto(yhteystiedot, apiKieli, NAME_WWW)[NAME_WWW],
            }),
            uiYhteystiedot
        );
    }, {} as Yhteystiedot);
    const osoitteetOnEri = kayntiOnEri(yhteysTiedot.fi) || kayntiOnEri(yhteysTiedot.sv);
    return { ...yhteysTiedot, osoitteetOnEri };
}

function mapApiVakaToUi({
    vaka: varhaiskasvatuksenToimipaikkaTiedot,
    koodistot: {
        vardatoimintamuotoKoodisto,
        vardakasvatusopillinenjarjestelmaKoodisto,
        vardatoiminnallinenpainotusKoodisto,
        vardajarjestamismuotoKoodisto,
        kielikoodisto,
    },
}) {
    if (!varhaiskasvatuksenToimipaikkaTiedot) return undefined;
    return {
        toimintamuoto: vardatoimintamuotoKoodisto.uri2SelectOption(varhaiskasvatuksenToimipaikkaTiedot.toimintamuoto),
        kasvatusopillinenJarjestelma: vardakasvatusopillinenjarjestelmaKoodisto.uri2SelectOption(
            varhaiskasvatuksenToimipaikkaTiedot.kasvatusopillinenJarjestelma
        ),
        paikkojenLukumaara: varhaiskasvatuksenToimipaikkaTiedot.paikkojenLukumaara,
        varhaiskasvatuksenToiminnallinenpainotukset: varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToiminnallinenpainotukset.map(
            (a) => ({
                painotus: vardatoiminnallinenpainotusKoodisto.uri2SelectOption(a.toiminnallinenpainotus),
                alkupvm: new Date(a.alkupvm),
                loppupvm: a.loppupvm ? new Date(a.loppupvm) : undefined,
            })
        ),
        varhaiskasvatuksenKielipainotukset: varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenKielipainotukset.map(
            (a) => {
                return {
                    painotus: kielikoodisto.uri2SelectOption(a.kielipainotus),
                    alkupvm: new Date(a.alkupvm),
                    loppupvm: a.loppupvm ? new Date(a.loppupvm) : undefined,
                };
            }
        ),
        varhaiskasvatuksenJarjestamismuodot: varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenJarjestamismuodot.map(
            (a) => vardajarjestamismuotoKoodisto.uri2SelectOption(a)
        ),
    };
}
const yhteysTietoReducer = (p, c) => {
    switch (c['YhteystietoArvo.kieli'].substr(0, 8)) {
        case 'kieli_fi':
            return { ...p, fi: c['YhteystietoArvo.arvoText'] };
        case 'kieli_sv':
            return { ...p, sv: c['YhteystietoArvo.arvoText'] };
        case 'kieli_en':
            return { ...p, en: c['YhteystietoArvo.arvoText'] };
        default:
            return { ...p };
    }
};
function mapApiYhteysTietoArvotToUi(yhteystietoArvos?: ApiYhteystietoArvo[]): YhteystietoArvot {
    return {
        koskiposti: (yhteystietoArvos || [])
            .filter((a) => {
                return a['YhteystietojenTyyppi.oid'] === KOSKIPOSTI_TYYPI_OID;
            })
            .reduce(yhteysTietoReducer, {}),
    };
}

function mapUiYhteystiedotToApi({
    postinumerotKoodisto,
    apiYhteystiedot = [],
    uiYhteystiedot,
}: {
    postinumerotKoodisto: Koodisto;
    apiYhteystiedot?: ApiYhteystiedot[];
    uiYhteystiedot: Yhteystiedot;
}): ApiYhteystiedot[] {
    const { osoitteetOnEri, ...rest } = uiYhteystiedot;
    return Object.keys(rest)
        .map((kieli) => {
            const apikieli = `kieli_${kieli}#1`;
            const postiosoite = getApiOsoite(apiYhteystiedot, apikieli, 'posti');
            postiosoite.osoite = uiYhteystiedot[kieli].postiOsoite;
            postiosoite.postinumeroUri = postinumerotKoodisto.arvo2Uri(uiYhteystiedot[kieli].postiOsoitePostiNro);
            postiosoite.postitoimipaikka = uiYhteystiedot[kieli].postiOsoiteToimipaikka;
            const kayntiosoite =
                osoitteetOnEri &&
                uiYhteystiedot[kieli].kayntiOsoite === '' &&
                uiYhteystiedot[kieli].kayntiOsoiteToimipaikka === '' &&
                uiYhteystiedot[kieli].kayntiOsoitePostiNro === ''
                    ? undefined
                    : getApiOsoite(apiYhteystiedot, apikieli, 'kaynti');
            if (
                kayntiosoite &&
                osoitteetOnEri === true &&
                !!uiYhteystiedot[kieli].kayntiOsoite &&
                !!uiYhteystiedot[kieli].kayntiOsoitePostiNro
            ) {
                kayntiosoite.osoite = uiYhteystiedot[kieli].kayntiOsoite;
                kayntiosoite.postinumeroUri = postinumerotKoodisto.arvo2Uri(uiYhteystiedot[kieli].kayntiOsoitePostiNro);
                kayntiosoite.postitoimipaikka = uiYhteystiedot[kieli].kayntiOsoiteToimipaikka;
            } else if (kayntiosoite && osoitteetOnEri === false) {
                kayntiosoite.osoite = postiosoite.osoite;
                kayntiosoite.postinumeroUri = postiosoite.postinumeroUri;
                kayntiosoite.postitoimipaikka = postiosoite.postitoimipaikka;
            }
            const puhelinnumero =
                uiYhteystiedot[kieli].puhelinnumero === ''
                    ? undefined
                    : (getApiYhteystieto(apiYhteystiedot, apikieli, NAME_PHONE) as YhteystiedotPhone);
            if (puhelinnumero && uiYhteystiedot[kieli].puhelinnumero) {
                puhelinnumero.tyyppi = 'puhelin';
                puhelinnumero[NAME_PHONE] = uiYhteystiedot[kieli].puhelinnumero;
            }
            const email = getApiYhteystieto(apiYhteystiedot, apikieli, NAME_EMAIL);
            if (uiYhteystiedot[kieli].email) {
                email[NAME_EMAIL] = uiYhteystiedot[kieli].email;
            }
            const www =
                uiYhteystiedot[kieli].www === '' ? undefined : getApiYhteystieto(apiYhteystiedot, apikieli, NAME_WWW);
            if (www && uiYhteystiedot[kieli].www) {
                www[NAME_WWW] = uiYhteystiedot[kieli].www;
            }
            return checkAndMapValuesToYhteystiedot(
                [postiosoite, kayntiosoite, puhelinnumero, email, www].filter(Boolean) as ApiYhteystiedot[]
            );
        })
        .reduce((a, b) => a.concat(b));
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
            return undefined;
        })
        .filter(Boolean) as ApiYhteystiedot[];
};

function transformData(data: APIOrganisaatioHistoria): OrganisaatioHistoria {
    function liitosMapper(a: APIOrganisaatioLiitos): OrganisaatioSuhde {
        return {
            alkuPvm: getUiDateStr(a.alkuPvm),
            loppuPvm: getUiDateStr(a.loppuPvm),
            child: a.kohde,
            parent: a.organisaatio,
        };
    }

    function suhdeMapper(a: APIOrganisaatioSuhde): OrganisaatioSuhde {
        return { ...a, alkuPvm: getUiDateStr(a.alkuPvm), loppuPvm: getUiDateStr(a.loppuPvm) };
    }

    return {
        childSuhteet: data.childSuhteet.map(suhdeMapper),
        parentSuhteet: data.parentSuhteet.map(suhdeMapper),
        liitokset: data.liitokset.map(liitosMapper),
        liittymiset: data.liittymiset.map(liitosMapper),
    };
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
    getApiOsoite,
    getApiYhteystieto,
    mapApiYhteystiedotToUi,
    mapApiVakaToUi,
    mapUiYhteystiedotToApi,
    mapApiYhteysTietoArvotToUi,
    mapUiOrganisaatioToApiToSave,
    mapUiOrganisaatioToApiToUpdate,
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
