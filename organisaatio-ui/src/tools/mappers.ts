import { Koodi, KoodistoSelectOption, Ryhma, SupportedKieli } from '../types/types';
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
    const kieliKortit: SupportedKieli[] = ['fi', 'sv', 'en'];
    const opetuskielet = opetuskieletOptions.map((kieliOption) => kieliOption.label);
    if (
        (opetuskielet.includes('suomi/ruotsi') ||
            (opetuskielet.includes('suomi') && opetuskielet.includes('ruotsi'))) &&
        opetuskielet.includes('muu')
    ) {
        return kieliKortit;
    } else if (opetuskielet.includes('suomi/ruotsi')) {
        return [kieliKortit[0], kieliKortit[1]];
    } else if (opetuskielet.includes('suomi') && !opetuskielet.includes('ruotsi')) {
        return [kieliKortit[0]];
    } else if (opetuskielet.includes('ruotsi') && !opetuskielet.includes('suomi')) {
        return [kieliKortit[1]];
    } else if (opetuskielet.includes('muu') && !opetuskielet.includes('ruotsi') && !opetuskielet.includes('suomi')) {
        return [kieliKortit[2]];
    } else {
        return [kieliKortit[0]];
    }
};
