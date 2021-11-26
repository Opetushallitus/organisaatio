import { Koodi, Ryhma, SupportedKieli, Yhteystiedot } from '../types/types';
export const dropKoodiVersionSuffix = (koodi: string) => {
    const hasVersioningHashtag = koodi.search('#');
    if (hasVersioningHashtag !== -1) {
        return koodi.replace(/#.+$/, '');
    }
    return koodi;
};

export const mapLocalizedKoodiToLang = (lang: string, property: string, value: Koodi | Ryhma) =>
    value[property][lang] || value[property].fi || value[property].sv || value[property].en || '';

//TODO tarttee katsoa ryhmissa tarvitaanko näitä vielä.

export const mapVisibleKieletFromOpetuskielet = (opetuskielet: string[] | undefined): SupportedKieli[] => {
    const priority = ['fi', 'sv', 'en'];
    const mapping = {
        suomi: ['fi'],
        ruotsi: ['sv'],
        'suomi/ruotsi': ['fi', 'sv'],
        englanti: ['en'],
        muu: ['en'],
    };
    console.log(mapping, 'mapping', opetuskielet);
    const sort = (input: SupportedKieli[]): SupportedKieli[] =>
        input.sort((a, b) => priority.indexOf(a) - priority.indexOf(b));
    return sort(
        Array.from(
            new Set(
                (opetuskielet?.length ? opetuskielet : ['suomi']).reduce(
                    (acc, lang) => [...acc, ...(mapping[lang] || [])],
                    [] as SupportedKieli[]
                )
            )
        )
    );
};

export const checkHasSomeValueByKieli = (KielisetYhteystiedot: Yhteystiedot[SupportedKieli]): boolean => {
    return (
        Object.keys(KielisetYhteystiedot).filter((yhteystietokentta) => KielisetYhteystiedot[yhteystietokentta])
            .length > 0
    );
};
