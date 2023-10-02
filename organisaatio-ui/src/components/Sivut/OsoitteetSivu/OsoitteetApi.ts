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
    postiosoite: string;
    kayntiosoite: string;
};

export async function haeOsoitteet(): Promise<Hakutulos[]> {
    const response = await axios.get<Hakutulos[]>(`${API_CONTEXT}/osoitteet/hae`);
    return response.data;
}
