import useAxios from 'axios-hooks';
import { Koodi } from '../types/types';

export default function useKoodisto(koodisto: string, onlyValid?: boolean) {
    const url = `/organisaatio/koodisto/${koodisto}/koodi` + (onlyValid ? '?onlyValid=true' : '');
    const [{ data, loading, error }] = useAxios<Koodi[]>(url);
    return { data, loading, error };
}
