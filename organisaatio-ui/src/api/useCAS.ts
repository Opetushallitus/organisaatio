import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { CASMe } from '../types/apiTypes';
import { Language } from '../types/types';

export function useCASLanguage() {
    const virkailija = urls.url('urlVirkailija');
    const url = `${virkailija}/kayttooikeus-service/cas/me`;
    const [{ data, loading, error }] = useAxios<CASMe>(url);
    if (error) {
        return { data: 'fi' as Language, loading: false, error: null };
    }
    return { data: data && data.lang, loading, error };
}
