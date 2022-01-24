import { Lokalisointi } from '../types/types';
import { API_CONTEXT } from '../contexts/constants';
import axios from 'axios';

const baseUrl = `${API_CONTEXT}/lokalisointi/`;

export const lokalisaatio = async () => {
    const { data } = await axios.get<Lokalisointi>(baseUrl);
    return data;
};
