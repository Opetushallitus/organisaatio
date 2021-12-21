import { Rakenne } from '../types/types';
import organisaatioRakenne from './organisaatioRakenne.json';
import { homepage } from '../../package.json';

export const ROOT_OID = '1.2.246.562.10.00000000001';
export const BASE_PATH = homepage;
export const API_CONTEXT = `${homepage}/internal`;
export const PUBLIC_API_CONTEXT = `${homepage}/api`;
export const LEGACY_API_CONTEXT = `${homepage}/rest`;
export const rakenne = organisaatioRakenne as Rakenne[];
