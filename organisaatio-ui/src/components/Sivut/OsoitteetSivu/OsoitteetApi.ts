import axios from 'axios';
import { API_CONTEXT } from '../../../contexts/constants';
import { KoodiUri } from '../../../types/types';

export type Hakutulos = {
    id: number;
    oid: string;
    nimi: string;
    sahkoposti?: string;
    yritysmuoto: string;
    puhelinnumero?: string;
    opetuskieli?: string;
    oppilaitostunnus?: string;
    kunta: string;
    koskiVirheilmoituksenOsoite?: string;
    ytunnus: string;
    postiosoite?: string;
    kayntiosoite?: string;
};

export type HaeRequest = {
    organisaatiotyypit: KoodiUri[];
    oppilaitostyypit: KoodiUri[];
    vuosiluokat: KoodiUri[];
    kunnat: KoodiUri[];
    anyJarjestamislupa: boolean;
    jarjestamisluvat: KoodiUri[];
    kielet: KoodiUri[];
};

export type KoodistoKoodi = {
    koodiUri: string;
    nimi: string;
};

export type MaakuntaKoodi = KoodistoKoodi & {
    kunnat: KoodiUri[];
};

export type OppilaitosRyhma = {
    nimi: string;
    koodit: string[];
};

export type HakuParametrit = {
    oppilaitostyypit: {
        koodit: KoodistoKoodi[];
        ryhmat: OppilaitosRyhma[];
    };
    vuosiluokat: KoodistoKoodi[];
    maakunnat: MaakuntaKoodi[];
    kunnat: KoodistoKoodi[];
    jarjestamisluvat: KoodistoKoodi[];
    kielet: KoodistoKoodi[];
};

export async function haeOsoitteet(request: HaeRequest): Promise<Hakutulos[]> {
    const response = await axios.post<Hakutulos[]>(`${API_CONTEXT}/osoitteet/hae`, request);
    return response.data;
}

export async function haeHakuParametrit() {
    const response = await axios.get<HakuParametrit>(`${API_CONTEXT}/osoitteet/parametrit`);
    return response.data;
}
