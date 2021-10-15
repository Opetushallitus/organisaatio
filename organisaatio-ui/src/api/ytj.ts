import Axios from 'axios';
import { isYTunnus } from '../tools/ytj';
import { YtjHaku, YtjOrganisaatio } from '../types/apiTypes';
import { errorHandlingWrapper } from './errorHandling';

const baseUrl = `/organisaatio/ytj/`;

async function getByYTunnus(yTunnus: string): Promise<YtjOrganisaatio | undefined> {
    return errorHandlingWrapper(async () => {
        if (!isYTunnus(yTunnus)) {
            console.error('Function should only be called with valid y-tunnus.');
            return undefined;
        }
        const { data } = await Axios.get<YtjOrganisaatio>(`${baseUrl}/${yTunnus}`);
        return data;
    });
}
async function searchByName(name: string): Promise<YtjHaku[]> {
    return errorHandlingWrapper(async () => {
        const { data } = await Axios.get<YtjOrganisaatio[]>(`${baseUrl}/hae`, { params: { nimi: name } });
        return data;
    });
}
export { getByYTunnus, searchByName };
