import { Rekisterointi } from '../types/types';

export enum Tila {
    KASITTELYSSA = 'KASITTELYSSA',
    HYVAKSYTTY = 'HYVAKSYTTY',
    HYLATTY = 'HYLATTY',
}

export type Paatos = {
    hyvaksytty: boolean;
    aikaleima: string;
    perustelu?: string; // vain, jos HYLATTY
};

export interface Rekisterointihakemus extends Rekisterointi {
    id: number;
    vastaanotettu: string;
    tila: Tila;
    paatos?: Paatos; // puuttuu, jos tila on KASITTELYSSA
}
