import {Rekisterointi} from "../types";

export enum Tila {
    KASITTELYSSA = "KASITTELYSSA",
    HYVAKSYTTY = "HYVAKSYTTY",
    HYLATTY = "HYLATTY"
}

export type Paatos = {
    hyvaksytty: boolean;
    aikaleima: Date;
    perustelu?: string; // vain, jos HYLATTY
}

export interface Rekisterointihakemus extends Rekisterointi {
    id: number;
    vastaanotettu: string; // Date ongelmallinen JSONista deserialisoidessa :(
    tila: Tila;
    paatos?: Paatos; // puuttuu, jos tila on KASITTELYSSA
}
