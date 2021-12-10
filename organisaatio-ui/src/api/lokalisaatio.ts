import useAxios from 'axios-hooks';
import { Lokalisointi } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';

const baseUrl = `${API_CONTEXT}/lokalisointi/`;

export default function useLokalisaatio() {
    const [{ data, loading, error }] = useAxios<Lokalisointi>(baseUrl);
    return { data, loading, error };
}
