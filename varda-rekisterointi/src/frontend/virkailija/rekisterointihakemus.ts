import { Rekisterointi } from '../types/types';

export const TILAT = ['KASITTELYSSA', 'HYVAKSYTTY', 'HYLATTY'] as const;

export type Tila = (typeof TILAT)[number];

export type Paatos = {
    hyvaksytty: boolean;
    paatetty: string;
    perustelu?: string; // vain, jos HYLATTY
};

export interface Rekisterointihakemus extends Rekisterointi {
    id: number;
    vastaanotettu: string;
    tila: Tila;
    paatos?: Paatos; // puuttuu, jos tila on KASITTELYSSA
}
