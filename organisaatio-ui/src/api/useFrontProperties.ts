import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { FrontProperties } from '../types/types';

export default function useFrontProperties() {
    const url = '/organisaatio/config/frontProperties';
    const [{ data, loading, error }] = useAxios<FrontProperties>(url);
    if (data) urls.addProperties(data);
    return { loading, error };
}
