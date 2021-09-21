import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { CASMe } from '../types/apiTypes';
import { Language } from '../types/types';

export function useCASLanguage() {
    const virkailija = urls.url('urlVirkailija');
    const baseUrl = `${virkailija}/kayttooikeus-service/`;
    const [{ data, loading, error }] = useAxios<CASMe>(`${baseUrl}cas/me`);
    if (error) {
        return { data: 'fi' as Language, loading: false, error: null };
    }
    return { data: data && data.lang, loading, error };
}
