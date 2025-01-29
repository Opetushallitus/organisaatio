import { Koodi, Language } from './types';

export const findPostitoimipaikka = (postinumero: string, postinumerot: Koodi[], language: Language) => {
    const postinumeroUri = `posti_${postinumero}`;
    return postinumerot.find((p) => p.uri === postinumeroUri)?.nimi[language];
};
