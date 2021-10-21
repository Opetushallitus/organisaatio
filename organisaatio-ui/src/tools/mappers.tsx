import { Koodi, Perustiedot, Ryhma, UiOrganisaatioBase, Yhteystiedot } from '../types/types';
import { KoodistoContext } from '../contexts/contexts';
import {
    ApiOrganisaatio,
    ApiYhteystiedot,
    OrganisaatioBase,
    YhteystiedotEmail,
    YhteystiedotOsoite,
    YhteystiedotPhone,
    YhteystiedotWww,
} from '../types/apiTypes';
import { useContext } from 'react';

export const dropKoodiVersionSuffix = (koodi: string) => {
    const hasVersioningHashtag = koodi.search('#');
    if (hasVersioningHashtag !== -1) {
        return koodi.replace(/#.+$/, '');
    }
    return koodi;
};

export const mapLocalizedKoodiToLang = (lang: string, property: string, value: Koodi | Ryhma) =>
    value[property][lang] || value[property].fi || value[property].sv || value[property].en || '';

type SupportedOsoiteType = 'kaynti' | 'posti';
type SupportedYhteystietoType = 'www' | 'email' | 'numero';

const NAME_WWW = 'www';
const NAME_EMAIL = 'email';
const NAME_PHONE = 'numero';

const initializeOsoite = (kieli: string, osoiteTyyppi: SupportedOsoiteType): YhteystiedotOsoite => ({
    kieli,
    osoiteTyyppi,
    postinumeroUri: '',
    postitoimipaikka: '',
    osoite: '',
    isNew: true,
});

const isOsoite = (yhteystieto: ApiYhteystiedot): yhteystieto is YhteystiedotOsoite =>
    yhteystieto.hasOwnProperty('osoiteTyyppi');

export const getOsoite = (
    yhteystiedot: ApiYhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedOsoiteType
): YhteystiedotOsoite => {
    const found = yhteystiedot.find(
        (yhteystieto: ApiYhteystiedot) =>
            isOsoite(yhteystieto) && yhteystieto.kieli === kieli && yhteystieto.osoiteTyyppi === osoiteTyyppi
    );
    if (found) {
        return found as YhteystiedotOsoite;
    }
    yhteystiedot.push(initializeOsoite(kieli, osoiteTyyppi));
    return getOsoite(yhteystiedot, kieli, osoiteTyyppi);
};

export const getYhteystieto = (
    yhteystiedot: ApiYhteystiedot[],
    kieli: string,
    osoiteTyyppi: SupportedYhteystietoType
): ApiYhteystiedot => {
    const found = yhteystiedot.find(
        (yhteystieto: ApiYhteystiedot) => yhteystieto.kieli === kieli && yhteystieto.hasOwnProperty(osoiteTyyppi)
    );
    if (found) {
        return found as ApiYhteystiedot;
    }
    yhteystiedot.push({ kieli, [osoiteTyyppi]: '', isNew: true } as ApiYhteystiedot);
    return getYhteystieto(yhteystiedot, kieli, osoiteTyyppi);
};

export const mapApiYhteystiedotToUi = (
    yhteystiedot: ApiYhteystiedot[],
    kielet = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1']
): Yhteystiedot => {
    return {
        ...kielet.reduce(
            (uiYhteystiedot, kieli) => (
                (uiYhteystiedot[kieli] = {
                    postiOsoite: getOsoite(yhteystiedot, kieli, 'posti').osoite,
                    postiOsoitePostiNro: getOsoite(yhteystiedot, kieli, 'posti').postinumeroUri,
                    postiOsoiteToimipaikka: getOsoite(yhteystiedot, kieli, 'posti').postitoimipaikka,
                    kayntiOsoite: getOsoite(yhteystiedot, kieli, 'kaynti').osoite,
                    kayntiOsoitePostiNro: getOsoite(yhteystiedot, kieli, 'kaynti').postinumeroUri,
                    kayntiOsoiteToimipaikka: getOsoite(yhteystiedot, kieli, 'kaynti').postitoimipaikka,
                    puhelinnumero: getYhteystieto(yhteystiedot, kieli, NAME_PHONE)[NAME_PHONE],
                    email: getYhteystieto(yhteystiedot, kieli, NAME_EMAIL)[NAME_EMAIL],
                    www: getYhteystieto(yhteystiedot, kieli, NAME_WWW)[NAME_WWW],
                }),
                uiYhteystiedot
            ),
            {} as Yhteystiedot
        ),
        osoitteetOnEri: false,
    };
};

export const mapUiYhteystiedotToApi = (
    apiYhteystiedot: ApiYhteystiedot[] = [],
    uiYhteystiedot: Yhteystiedot
): ApiYhteystiedot[] => {
    const { osoitteetOnEri, ...rest } = uiYhteystiedot;
    return Object.keys(rest)
        .map((kieli) => {
            const postiosoite = getOsoite(apiYhteystiedot, kieli, 'posti');
            postiosoite.osoite = uiYhteystiedot[kieli].postiOsoite;
            postiosoite.postinumeroUri = uiYhteystiedot[kieli].postiOsoitePostiNro;
            postiosoite.postitoimipaikka = uiYhteystiedot[kieli].postiOsoiteToimipaikka;
            const kayntiosoite = getOsoite(apiYhteystiedot, kieli, 'kaynti');
            if (
                uiYhteystiedot.osoitteetOnEri === true &&
                !!uiYhteystiedot[kieli].kayntiOsoite &&
                !!uiYhteystiedot[kieli].kayntiOsoitePostiNro
            ) {
                kayntiosoite.osoite = uiYhteystiedot[kieli].kayntiOsoite;
                kayntiosoite.postinumeroUri = uiYhteystiedot[kieli].kayntiOsoitePostiNro;
                kayntiosoite.postitoimipaikka = uiYhteystiedot[kieli].kayntiOsoiteToimipaikka;
            } else if (uiYhteystiedot.osoitteetOnEri === false) {
                kayntiosoite.osoite = uiYhteystiedot[kieli].postiOsoite;
                kayntiosoite.postinumeroUri = uiYhteystiedot[kieli].postiOsoitePostiNro;
                kayntiosoite.postitoimipaikka = uiYhteystiedot[kieli].postiOsoiteToimipaikka;
            }
            const puhelinnumero = getYhteystieto(apiYhteystiedot, kieli, NAME_PHONE) as YhteystiedotPhone;
            if (!!uiYhteystiedot[kieli].puhelinnumero) {
                puhelinnumero.tyyppi = 'puhelin';
                puhelinnumero[NAME_PHONE] = uiYhteystiedot[kieli].puhelinnumero;
            }
            const email = getYhteystieto(apiYhteystiedot, kieli, NAME_EMAIL);
            if (!!uiYhteystiedot[kieli].email) {
                email[NAME_EMAIL] = uiYhteystiedot[kieli].email;
            }
            const www = getYhteystieto(apiYhteystiedot, kieli, NAME_WWW);
            if (!!uiYhteystiedot[kieli].www) {
                www[NAME_WWW] = uiYhteystiedot[kieli].www;
            }
            return checkAndMapValuesToYhteystiedot([postiosoite, kayntiosoite, puhelinnumero, email, www]);
        })
        .reduce((a, b) => a.concat(b)) as ApiYhteystiedot[];
};

const checkAndMapValuesToYhteystiedot = (yhteystiedotObjectsArray: ApiYhteystiedot[]) => {
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
        .filter(Boolean);
};
