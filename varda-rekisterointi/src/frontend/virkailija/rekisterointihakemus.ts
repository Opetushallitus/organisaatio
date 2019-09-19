import {Rekisterointi} from "../types";

export enum Tila {
    KASITTELYSSA,
    HYVAKSYTTY,
    HYLATTY
}

export type Paatos = {
    hyvaksytty: boolean;
    aikaleima: Date;
    perustelu?: string; // vain, jos HYLATTY
}

export interface Rekisterointihakemus extends Rekisterointi {
    id: number;
    saapumisaika: Date;
    tila: Tila;
    paatos?: Paatos; // puuttuu, jos tila on KASITTELYSSA
}
