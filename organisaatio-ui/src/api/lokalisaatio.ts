import { Lokalisointi } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';
import { atom } from 'jotai';
import { casMeAtom } from './kayttooikeus';
import { I18nImpl } from '../contexts/LanguageContext';

const baseUrl = `${API_CONTEXT}/lokalisointi/`;

const lokalisaatio = async () => {
    const { data } = await axios.get<Lokalisointi>(baseUrl);
    return data;
};

export const languageAtom = atom(async (get) => {
    const lokals = await lokalisaatio();
    const casData = get(casMeAtom);
    return new I18nImpl(lokals, casData.lang);
});
