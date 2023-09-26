import axios from 'axios';
import { API_CONTEXT } from '../../../contexts/constants';

export type Hakutulos = {
    id: number;
    nimi: string;
    sahkoposti?: string;
};

export async function haeOsoitteet(): Promise<Hakutulos[]> {
    const response = await axios.get<Hakutulos[]>(`${API_CONTEXT}/osoitteet/hae`);
    return response.data;
}
