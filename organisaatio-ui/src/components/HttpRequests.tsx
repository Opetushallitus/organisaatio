import { NewRyhma, Ryhma } from '../types/types';
import Axios, { AxiosResponse } from 'axios';

const getResponseData = (response: AxiosResponse) => response.data;

export const putRyhma = async (ryhma: Ryhma) =>
    getResponseData(await Axios.put(`/organisaatio/organisaatio/v4/${ryhma.oid}`, ryhma));
export const postRyhma = async (ryhma: NewRyhma) =>
    getResponseData(await Axios.post(`/organisaatio/organisaatio/v4/`, ryhma));
export const deleteRyhma = async (ryhma: Ryhma) =>
    getResponseData(await Axios.delete(`/organisaatio/organisaatio/v4/${ryhma.oid}`));
export const getRyhma = async (oid: string) =>
    getResponseData(await Axios.get(`/organisaatio/organisaatio/v4/${oid}?includeImage=true`));
export const getRyhmat = async () =>
    getResponseData(await Axios.get(`/organisaatio/organisaatio/v3/ryhmat?aktiivinen=true`));
