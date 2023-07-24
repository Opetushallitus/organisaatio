import { FrontProperties } from '../types/types';
import { createContext, useContext } from 'react';
import axios from 'axios';
import { API_CONTEXT } from './constants';

export async function fetchFrontProperties() {
    const response = await axios.get<FrontProperties>(`${API_CONTEXT}/config/frontproperties`);
    return response.data;
}

export const FrontPropertiesContext = createContext<FrontProperties | undefined>(undefined);
export const useFrontProperties = () => useContext(FrontPropertiesContext);
