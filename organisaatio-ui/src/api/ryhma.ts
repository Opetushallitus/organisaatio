import { NewRyhma, Ryhma } from '../types/types';
import Axios, { AxiosResponse } from 'axios';
import { LEGACY_API_CONTEXT, PUBLIC_API_CONTEXT } from '../contexts/constants';

const baseUrl = `${PUBLIC_API_CONTEXT}/`;

const getResponseData = (response: AxiosResponse) => response.data;

export const putRyhma = async (ryhma: Ryhma) => getResponseData(await Axios.put(`${baseUrl}${ryhma.oid}`, ryhma));
export const postRyhma = async (ryhma: NewRyhma) => getResponseData(await Axios.post(`${baseUrl}`, ryhma));
export const deleteRyhma = async (ryhma: Ryhma) => getResponseData(await Axios.delete(`${baseUrl}${ryhma.oid}`));
export const getRyhma = async (oid: string) => getResponseData(await Axios.get(`${baseUrl}${oid}?includeImage=true`));
export const getRyhmat = async () =>
    getResponseData(await Axios.get(`${LEGACY_API_CONTEXT}/organisaatio/v3/ryhmat?aktiivinen=true`));
