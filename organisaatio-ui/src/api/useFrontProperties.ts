import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
export default function useFrontProperties() {
    const url = '/organisaatio/config/frontProperties';
    const [{ data, loading, error }] = useAxios<any>(url);
    if (data) urls.addProperties(data);
    return { loading, error };
}
