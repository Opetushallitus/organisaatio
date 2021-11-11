import useAxios from 'axios-hooks';
import { Koodi } from '../types/types';
import { LEGACY_API_CONTEXT } from '../contexts/contexts';
const baseUrl = `${LEGACY_API_CONTEXT}/koodisto/`;

export default function useKoodisto(koodisto: string, onlyValid?: boolean) {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const [{ data, loading, error }] = useAxios<Koodi[]>(url);
    return { data, loading, error };
}
