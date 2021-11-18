import { Koodi, KoodistoSelectOption, Ryhma, SupportedKieli, Yhteystiedot } from '../types/types';
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

export const mapVisibleKieletFromOpetuskielet = (opetuskieletOptions: KoodistoSelectOption[]): SupportedKieli[] => {
    const opetuskielet = opetuskieletOptions.map((kieliOption) => kieliOption.label);
    const visibleKielet = new Set<SupportedKieli>();
    if (
        (opetuskielet.includes('suomi/ruotsi') ||
            (opetuskielet.includes('suomi') && opetuskielet.includes('ruotsi'))) &&
        opetuskielet.includes('muu')
    ) {
        visibleKielet.add('fi').add('sv').add('en');
    } else if (opetuskielet.includes('suomi/ruotsi')) {
        visibleKielet.add('fi').add('sv');
    } else if (opetuskielet.includes('suomi') && !opetuskielet.includes('ruotsi')) {
        visibleKielet.add('fi');
    } else if (opetuskielet.includes('ruotsi') && !opetuskielet.includes('suomi')) {
        visibleKielet.add('sv');
    } else if (opetuskielet.includes('muu') && !opetuskielet.includes('ruotsi') && !opetuskielet.includes('suomi')) {
        visibleKielet.add('en');
    } else {
        visibleKielet.add('fi');
    }
    return Array.from(visibleKielet);
};

export const checkHasSomeValueByKieli = (KielisetYhteystiedot: Yhteystiedot[SupportedKieli]): boolean => {
    return (
        Object.keys(KielisetYhteystiedot).filter((yhteystietokentta) => KielisetYhteystiedot[yhteystietokentta])
            .length > 0
    );
};
