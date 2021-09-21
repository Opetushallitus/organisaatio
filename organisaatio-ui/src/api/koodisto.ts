import useAxios from 'axios-hooks';
import { Koodi } from '../types/types';
const baseUrl = '/organisaatio/koodisto/';

export default function useKoodisto(koodisto: string, onlyValid?: boolean) {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const [{ data, loading, error }] = useAxios<Koodi[]>(url);
    return { data, loading, error };
}
