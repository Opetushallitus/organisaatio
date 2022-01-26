import Axios from 'axios';
import { isYTunnus } from '../tools/ytj';
import { errorHandlingWrapper } from './errorHandling';
import { KoodistoSelectOption, Koodistot, LocalDate, YhteystiedotBase } from '../types/types';
import { LEGACY_API_CONTEXT } from '../contexts/constants';
import moment from 'moment';

const baseUrl = `${LEGACY_API_CONTEXT}/ytj/`;

type ytjOsoite = {
    katu: string;
    postinumero: string;
    toimipaikka: string;
    maa: string;
    kieli: boolean;
};

type ytjYtunnus = {
    status?: string;
    alkupvm: LocalDate;
    loppupvm: LocalDate;
    yritysLopetettu: boolean;
    ytunnus: string;
};

export type YtjData = Omit<YtjAPIData, 'aloitusPvm'> & {
    aloitusPvm: LocalDate;
    kunta?: KoodistoSelectOption;
    kieli?: KoodistoSelectOption;
    yhteysTiedot: YhteystiedotBase;
};
export type YtjHaku = {
    ytunnus: string;
    nimi: string;
};
type YtjDate = `${number}${number}-${number}${number}-${number}${number}${number}${number}`;
type YtjAPIData = YtjHaku & {
    aloitusPvm: YtjDate;
    yritysmuoto: string;
    yritysmuotoKoodi: string;
    kayntiOsoite?: ytjOsoite;
    kotiPaikka: string;
    kotiPaikkaKoodi: string;
    postiOsoite: ytjOsoite;
    puhelin: string;
    sahkoposti: string;
    www: string;
    toimiala: string;
    toimialaKoodi?: string;
    versio?: boolean;
    yrityksenKieli?: string;
    yritysTunnus: ytjYtunnus;
};

const mapApiToUI = (ytj: YtjAPIData, koodistot: Koodistot): YtjData => {
    const { kuntaKoodisto, oppilaitoksenOpetuskieletKoodisto } = koodistot;
    const selectedKunta = kuntaKoodisto.koodit().find((a) => a.arvo === ytj.kotiPaikkaKoodi);
    const selectedKuntaSelector = kuntaKoodisto
        .selectOptions()
        .find((a) => a.value.startsWith(selectedKunta?.uri || ''));
    const selectedKieli = oppilaitoksenOpetuskieletKoodisto
        .selectOptions()
        .find((a) => a.label === ytj.yrityksenKieli?.toLowerCase());
    return {
        ...ytj,
        kunta: selectedKuntaSelector,
        kieli: selectedKieli,
        aloitusPvm: moment(ytj.aloitusPvm, 'DD-MM-YYYY').format('YYYY-MM-DD') as LocalDate,
        yhteysTiedot: {
            postiOsoite: ytj.postiOsoite.katu,
            postiOsoitePostiNro: ytj.postiOsoite.postinumero,
            postiOsoiteToimipaikka: ytj.postiOsoite.toimipaikka,
            kayntiOsoite: ytj.kayntiOsoite?.katu || ytj.postiOsoite.katu,
            kayntiOsoitePostiNro: ytj.kayntiOsoite?.postinumero || ytj.postiOsoite.postinumero,
            kayntiOsoiteToimipaikka: ytj.kayntiOsoite?.toimipaikka || ytj.postiOsoite.toimipaikka,
            puhelinnumero: ytj.puhelin,
            email: ytj.sahkoposti,
            www: ytj.www,
        },
    };
};

async function getByYTunnus(yTunnus: string, koodistot: Koodistot): Promise<YtjData | undefined> {
    return errorHandlingWrapper(async () => {
        if (!isYTunnus(yTunnus)) {
            console.error('Function should only be called with valid y-tunnus.');
            return undefined;
        }
        const { data } = await Axios.get<YtjAPIData>(`${baseUrl}${yTunnus}`);
        return mapApiToUI(data, koodistot);
    });
}

async function searchByName(name: string): Promise<YtjHaku[] | undefined> {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.get<YtjHaku[]>(`${baseUrl}hae`, { params: { nimi: name } });
        return data;
    });
}

const isYtjData = (input: YtjHaku | YtjData): input is YtjData => {
    if ((input as YtjData).yritysTunnus) return true;
    return false;
};

export { getByYTunnus, searchByName, isYtjData };
