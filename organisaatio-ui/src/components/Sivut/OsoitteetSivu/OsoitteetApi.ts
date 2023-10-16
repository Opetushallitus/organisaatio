import axios from 'axios';
import { API_CONTEXT } from '../../../contexts/constants';

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

type Koodiarvo = string;

export type HaeRequest = {
    organisaatiotyypit: Koodiarvo[];
    oppilaitostyypit: Koodiarvo[];
    vuosiluokat: Koodiarvo[];
};

export type KoodistoKoodi = {
    koodiUri: string;
    nimi: string;
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
};

export async function haeOsoitteet(req: HaeRequest): Promise<Hakutulos[]> {
    const params = req;
    const response = await axios.get<Hakutulos[]>(`${API_CONTEXT}/osoitteet/hae`, { params });
    return response.data;
}

export async function haeHakuParametrit() {
    const response = await axios.get<HakuParametrit>(`${API_CONTEXT}/osoitteet/parametrit`);
    return response.data;
}
