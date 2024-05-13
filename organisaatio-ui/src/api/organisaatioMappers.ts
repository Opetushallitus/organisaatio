import {
    Koodisto,
    OrganisaatioHistoria,
    OrganisaatioSuhde,
    Perustiedot,
    UiOrganisaatioBase,
    Yhteystiedot,
    YhteystiedotBase,
    YhteystietoArvot,
} from '../types/types';
import {
    ApiOrganisaatio,
    APIOrganisaatioHistoria,
    APIOrganisaatioLiitos,
    APIOrganisaatioSuhde,
    ApiVakaTiedot,
    ApiYhteystiedot,
    ApiYhteystietoArvo,
    NewApiOrganisaatio,
    YhteystiedotEmail,
    YhteystiedotOsoite,
    YhteystiedotPhone,
    YhteystiedotWww,
} from '../types/apiTypes';
import { KOSKIPOSTI_BASE, KOSKIPOSTI_TYYPI_OID, ROOT_OID } from '../contexts/constants';
import { UnpackNestedValue } from 'react-hook-form';
import { formatUiDateStrToApi, getUiDateStr } from '../tools/mappers';

const NAME_WWW = 'www';
const NAME_EMAIL = 'email';
const NAME_PHONE = 'numero';

type SupportedOsoiteType = 'kaynti' | 'posti';
type SupportedYhteystietoType = 'www' | 'email' | 'numero';

export function mapUiOrganisaatioToApiToSave(
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

function mapArvot(
    yhteystietoArvoFormValuet: YhteystietoArvot,
    field: keyof YhteystietoArvot,
    base: typeof KOSKIPOSTI_BASE
) {
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

export function mapUiOrganisaatioToApiToUpdate(
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

export function mapApiYhteystiedotToUi(
    postinumerotKoodisto: Koodisto,
    yhteystiedot: ApiYhteystiedot[] = [],
    kielet = ['fi', 'sv', 'en'] as const
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
                puhelinnumero: (getApiYhteystieto(yhteystiedot, apiKieli, NAME_PHONE) as YhteystiedotPhone)[NAME_PHONE],
                email: (getApiYhteystieto(yhteystiedot, apiKieli, NAME_EMAIL) as YhteystiedotEmail)[NAME_EMAIL],
                www: (getApiYhteystieto(yhteystiedot, apiKieli, NAME_WWW) as YhteystiedotWww)[NAME_WWW],
            }),
            uiYhteystiedot
        );
    }, {} as Yhteystiedot);
    const osoitteetOnEri = kayntiOnEri(yhteysTiedot.fi) || kayntiOnEri(yhteysTiedot.sv);
    return { ...yhteysTiedot, osoitteetOnEri };
}

type ApiVaka = {
    vaka?: ApiVakaTiedot;
    koodistot: {
        vardatoimintamuotoKoodisto: Koodisto;
        vardakasvatusopillinenjarjestelmaKoodisto: Koodisto;
        vardatoiminnallinenpainotusKoodisto: Koodisto;
        vardajarjestamismuotoKoodisto: Koodisto;
        kielikoodisto: Koodisto;
    };
};

export function mapApiVakaToUi({
    vaka: varhaiskasvatuksenToimipaikkaTiedot,
    koodistot: {
        vardatoimintamuotoKoodisto,
        vardakasvatusopillinenjarjestelmaKoodisto,
        vardatoiminnallinenpainotusKoodisto,
        vardajarjestamismuotoKoodisto,
        kielikoodisto,
    },
}: ApiVaka) {
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
const yhteysTietoReducer = (
    p: { fi?: string; sv?: string; en?: string },
    c: { 'YhteystietoArvo.kieli': string; 'YhteystietoArvo.arvoText'?: string }
) => {
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
export function mapApiYhteysTietoArvotToUi(yhteystietoArvos?: ApiYhteystietoArvo[]): YhteystietoArvot {
    return {
        koskiposti: (yhteystietoArvos || [])
            .filter((a) => {
                return a['YhteystietojenTyyppi.oid'] === KOSKIPOSTI_TYYPI_OID;
            })
            .reduce(yhteysTietoReducer, {}),
    };
}

export function mapUiYhteystiedotToApi({
    postinumerotKoodisto,
    apiYhteystiedot = [],
    uiYhteystiedot,
}: {
    postinumerotKoodisto: Koodisto;
    apiYhteystiedot?: ApiYhteystiedot[];
    uiYhteystiedot: Yhteystiedot;
}): ApiYhteystiedot[] {
    const { osoitteetOnEri } = uiYhteystiedot;
    return (['fi', 'sv', 'en'] as const)
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
            const email = getApiYhteystieto(apiYhteystiedot, apikieli, NAME_EMAIL) as YhteystiedotEmail;
            if (uiYhteystiedot[kieli].email) {
                email[NAME_EMAIL] = uiYhteystiedot[kieli].email;
            }
            const www =
                uiYhteystiedot[kieli].www === ''
                    ? undefined
                    : (getApiYhteystieto(apiYhteystiedot, apikieli, NAME_WWW) as YhteystiedotWww);
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

export function transformData(data: APIOrganisaatioHistoria): OrganisaatioHistoria {
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

export function getApiOsoite(
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

export function getApiYhteystieto(
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
