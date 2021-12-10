import useAxios from 'axios-hooks';
import { urls } from 'oph-urls-js';
import { CASMe } from '../types/apiTypes';
import { Language } from '../types/types';

type CASMeApi = {
    uid: string;
    oid: string;
    firstName: string;
    lastName: string;
    groups: string[];
    roles: string;
    lang: string;
};

export const mapApiToUI = (api: CASMeApi): CASMe => {
    return { ...api, roles: JSON.parse(api?.roles || '[]'), lang: (api?.lang || 'fi') as Language };
};

export function useCAS(): { data: CASMe; loading: boolean; error: undefined } {
    const virkailija = urls.url('urlVirkailija');
    const baseUrl = `${virkailija}/kayttooikeus-service/`;
    const [{ data, loading, error }] = useAxios<CASMeApi>(`${baseUrl}cas/me`);
    if (error) {
        return {
            data: {
                uid: '',
                oid: '',
                firstName: '',
                lastName: '',
                groups: [],
                roles: [],
                lang: 'fi' as Language,
            },
            loading: false,
            error: undefined,
        };
    }
    return { data: mapApiToUI(data), loading, error };
}
