import { FrontProperties } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import { atom } from 'jotai';
import axios from 'axios';

const baseUrl = `${API_CONTEXT}/config/`;

const urlAtom = atom(`${baseUrl}frontproperties`);
export const frontPropertiesAtom = atom(async (get) => {
    const response = await axios.get<FrontProperties>(get(urlAtom));
    return response.data;
});
