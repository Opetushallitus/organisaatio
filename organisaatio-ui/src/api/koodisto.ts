import useAxios from 'axios-hooks';
import { Koodi } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';

const baseUrl = `${API_CONTEXT}/koodisto/`;

export default function useKoodisto(koodisto: string, onlyValid?: boolean) {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const [{ data, loading, error }] = useAxios<Koodi[]>(url);
    return { data, loading, error };
}

export const koodisto = async (koodisto: string, onlyValid?: boolean): Promise<Koodi[]> => {
    const validParameter = onlyValid ? '?onlyValid=true' : '';
    const url = `${baseUrl}${koodisto}/koodi${validParameter}`;
    const response = await axios.get<Koodi[]>(url);
    return response.data;
};
