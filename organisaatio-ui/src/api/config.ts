import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { FrontProperties } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
const baseUrl = `${API_CONTEXT}/config/`;

export default function useFrontProperties() {
    const [{ data, loading, error }] = useAxios<FrontProperties>(`${baseUrl}frontproperties`);
    if (error) {
        urls.addProperties({ urlVirkailija: 'http://localhost:9000' });
        return { loading: false, error: null };
    }
    if (data) urls.addProperties(data);
    return { loading, error };
}
