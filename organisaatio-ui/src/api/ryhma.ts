import { NewRyhma, Ryhma } from '../types/types';
import Axios, { AxiosResponse } from 'axios';

const baseUrl = `/organisaatio/organisaatio/v4/`;

const getResponseData = (response: AxiosResponse) => response.data;

export const putRyhma = async (ryhma: Ryhma) => getResponseData(await Axios.put(`${baseUrl}${ryhma.oid}`, ryhma));
export const postRyhma = async (ryhma: NewRyhma) => getResponseData(await Axios.post(`${baseUrl}`, ryhma));
export const deleteRyhma = async (ryhma: Ryhma) => getResponseData(await Axios.delete(`${baseUrl}${ryhma.oid}`));
export const getRyhma = async (oid: string) => getResponseData(await Axios.get(`${baseUrl}${oid}?includeImage=true`));
export const getRyhmat = async () =>
    getResponseData(await Axios.get(`/organisaatio/organisaatio/v3/ryhmat?aktiivinen=true`));