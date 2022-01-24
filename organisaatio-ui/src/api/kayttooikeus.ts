import { CASMe, Language } from '../types/types';
import { CASMeImpl } from '../contexts/CasMeContext';
import axios from 'axios';

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
    return new CASMeImpl({ ...api, roles: JSON.parse(api?.roles || '[]'), lang: (api?.lang || 'fi') as Language });
};

export const cas = async (url) => {
    const { data } = await axios.get<CASMeApi>(`${url}cas/me`);
    return mapApiToUI(data);
};
