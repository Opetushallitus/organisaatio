import { Yhteystieto } from "./types";

export const getYhteystietoArvo = (yhteystiedot: Yhteystieto[] = [],
                                   filter: (yhteystieto: Yhteystieto) => boolean,
                                   mapper: (yhteystieto: Yhteystieto) => string): string => {
    const yhteystieto = yhteystiedot.find(filter);
    if (!yhteystieto) {
        return '';
    }
    return mapper(yhteystieto) || '';
}

export const updateYhteystiedot = (yhteystiedot: Yhteystieto[] = [],
                                   filter: (yhteystieto: Yhteystieto) => boolean,
                                   values: Yhteystieto): Yhteystieto[] => {
    let yhteystieto = yhteystiedot.find(filter);
    if (!yhteystieto) {
        yhteystieto = {};
    }
    const next = yhteystiedot.filter(yhteystieto => !filter(yhteystieto));
    next.push({ ...yhteystieto, ...values });
    return next;
}

export const isPuhelinnumero = (yhteystieto: Yhteystieto): boolean => !!yhteystieto.numero;
export const isSahkoposti = (yhteystieto: Yhteystieto): boolean => !!yhteystieto.email;
export const isKayntiosoite = (yhteystieto: Yhteystieto): boolean => yhteystieto.osoiteTyyppi === 'kaynti';
export const isPostiosoite = (yhteystieto: Yhteystieto): boolean => yhteystieto.osoiteTyyppi === 'posti';

export const toPuhelinnumero = (yhteystieto: Yhteystieto) => yhteystieto.numero;
export const toSahkoposti = (yhteystieto: Yhteystieto) => yhteystieto.email;
export const toOsoite = (yhteystieto: Yhteystieto) => yhteystieto.osoite;
export const toPostinumeroUri = (yhteystieto: Yhteystieto) => yhteystieto.postinumeroUri;
export const toPostitoimipaikka = (yhteystieto: Yhteystieto) => yhteystieto.postitoimipaikka;
