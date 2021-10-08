import useAxios from 'axios-hooks';
import { Lokalisointi } from '../types/types';

const baseUrl = '/organisaatio/lokalisointi/';

export default function useLokalisaatio() {
    const [{ data, loading, error }] = useAxios<Lokalisointi>(baseUrl);
    return { data, loading, error };
}
