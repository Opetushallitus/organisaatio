import { Rakenne } from '../types/types';
import organisaatioRakenne from './organisaatioRakenne.json';

export const ROOT_OID = '1.2.246.562.10.00000000001';
export const BASE_PATH = '/organisaatio-service';
export const API_CONTEXT = `/organisaatio-service/internal`;
export const PUBLIC_API_CONTEXT = `/organisaatio-service/api`;
export const LEGACY_API_CONTEXT = `/organisaatio-service/rest`;
export const rakenne = organisaatioRakenne as Rakenne[];
export const KOSKIPOSTI_TYYPI_OID = '1.2.246.562.5.79385887983';
export const KOSKIPOSTI_BASE = {
    'YhteystietojenTyyppi.oid': KOSKIPOSTI_TYYPI_OID,
    'YhteystietoElementti.oid': '1.2.246.562.5.57850489428',
    'YhteystietoElementti.pakollinen': false,
    'YhteystietoElementti.kaytossa': true,
};
