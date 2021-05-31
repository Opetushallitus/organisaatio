import { Ryhma } from '../types/types';
import Axios from 'axios';

export const putRyhma = async (ryhma: Ryhma) => await Axios.put(`/organisaatio/organisaatio/v4/${ryhma.oid}`, ryhma);
export const deleteRyhma = async (ryhma: Ryhma) => await Axios.delete(`/organisaatio/organisaatio/v4/${ryhma.oid}`);
export const getRyhma = async (oid: string) =>
    await Axios.get(`/organisaatio/organisaatio/v4/${oid}?includeImage=true`);
export const getRyhmat = async () => Axios.get(`/organisaatio/organisaatio/v3/ryhmat?aktiivinen=true`);
