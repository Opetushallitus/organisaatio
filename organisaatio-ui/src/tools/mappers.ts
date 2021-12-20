import { Koodi, Nimi, OrganisaationNimetNimi, Ryhma, SupportedKieli, Yhteystiedot } from '../types/types';
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
    const sort = (input: SupportedKieli[]): SupportedKieli[] =>
        input.sort((a, b) => priority.indexOf(a) - priority.indexOf(b));
    return sort(
        (opetuskielet?.length ? opetuskielet : ['suomi']).reduce(
            (acc, lang) => [...acc, ...(mapping[lang] || [])],
            [] as SupportedKieli[]
        )
    ).filter((value, index, self) => self.indexOf(value) === index);
};

export const checkHasSomeValueByKieli = (KielisetYhteystiedot: Yhteystiedot[SupportedKieli]): boolean => {
    return (
        Object.keys(KielisetYhteystiedot).filter((yhteystietokentta) => KielisetYhteystiedot[yhteystietokentta])
            .length > 0
    );
};

export const findCurrentNimi = (nimet: OrganisaationNimetNimi[], nimi: Nimi) => {
    const currentNimi = nimet.find((nimetNimi) => JSON.stringify(nimetNimi.nimi) === JSON.stringify(nimi));
    const editModeDisabled =
        currentNimi && !!nimet.find((nimetNimi) => new Date(nimetNimi.alkuPvm) > new Date(currentNimi.alkuPvm));
    return currentNimi ? { ...currentNimi, disabled: editModeDisabled } : undefined;
};
