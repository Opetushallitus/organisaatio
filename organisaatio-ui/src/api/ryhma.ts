import { NewRyhma, Ryhma } from '../types/types';
import axios, { AxiosResponse } from 'axios';
import { LEGACY_API_CONTEXT, PUBLIC_API_CONTEXT } from '../contexts/constants';
import { errorHandlingWrapper } from './errorHandling';

const baseUrl = `${PUBLIC_API_CONTEXT}/`;

const getResponseData = (response: AxiosResponse) => response.data;

export const putRyhma = async (ryhma: Ryhma) =>
    errorHandlingWrapper(async () => getResponseData(await axios.put(`${baseUrl}${ryhma.oid}`, ryhma)));
export const postRyhma = async (ryhma: NewRyhma) =>
    errorHandlingWrapper(async () => getResponseData(await axios.post(`${baseUrl}`, ryhma)));
export const deleteRyhma = async (ryhma: Ryhma) =>
    errorHandlingWrapper(async () => getResponseData(await axios.delete(`${baseUrl}${ryhma.oid}`)));
export const getRyhma = async (oid: string) =>
    errorHandlingWrapper(async () => getResponseData(await axios.get(`${baseUrl}${oid}?includeImage=true`)));

export const getRyhmat = async () =>
    errorHandlingWrapper(async () =>
        getResponseData(await axios.get(`${LEGACY_API_CONTEXT}/organisaatio/v3/ryhmat?aktiivinen=true`))
    );
