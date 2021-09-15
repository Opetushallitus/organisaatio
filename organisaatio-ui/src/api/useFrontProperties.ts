import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { FrontProperties } from '../types/types';

export default function useFrontProperties() {
    const url = '/organisaatio/config/frontproperties';
    const [{ data, loading, error }] = useAxios<FrontProperties>(url);
    if (error) {
        urls.addProperties({ urlVirkailija: 'http://localhost:9000' });
        return { loading: false, error: null };
    }
    if (data) urls.addProperties(data);
    return { loading, error };
}
