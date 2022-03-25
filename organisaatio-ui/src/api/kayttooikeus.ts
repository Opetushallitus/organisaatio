import { CASMe, Language } from '../types/types';
import { CASMeImpl } from '../contexts/CasMeContext';
import axios from 'axios';
import { atom } from 'jotai';
import { frontPropertiesAtom } from './config';

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

const urlAtom = atom((get) => `${get(frontPropertiesAtom).urlVirkailija}/kayttooikeus-service/`);
export const casMeAtom = atom(async (get) => {
    const { data } = await axios.get<CASMeApi>(`${get(urlAtom)}cas/me`);
    return mapApiToUI(data);
});
export const casMeLangAtom = atom((get) => {
    return get(casMeAtom).lang;
});
