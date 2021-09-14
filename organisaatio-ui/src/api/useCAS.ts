import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { CASMe } from '../types/apiTypes';

export function useCASLanguage() {
    const virkailija = urls.url('url-virkailija');
    const url = `${virkailija}/kayttooikeus-service/cas/me`;
    const [{ data, loading, error }] = useAxios<CASMe>(url);
    return { data: data && data.lang, loading, error };
}
