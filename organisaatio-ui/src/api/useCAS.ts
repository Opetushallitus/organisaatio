import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';

export function useCASLanguage() {
    let virkailija = 'http://localhost:9000';
    try {
        virkailija = urls.url('url-virkailija');
    } catch (error) {
        console.error('url-virkailija not set on server');
    }
    const url = `${virkailija}/kayttooikeus-service/cas/me`;
    const [{ data, loading, error }] = useAxios<any>(url);
    return { data: data && data.lang, loading, error };
}