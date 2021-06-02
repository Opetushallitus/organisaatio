import { Koodi } from '../types/types';

export const dropKoodiVersionSuffix = (koodi: string) => {
    const hasVersioningHashtag = koodi.search('#');
    if (hasVersioningHashtag !== -1) {
        return koodi.replace(/#.+$/, '');
    }
    return koodi;
};

export const mapLocalizedKoodiToLang = (lang: string, property: string, value: any) =>
    value[property][lang] || value[property].fi || value[property].sv || value[property].en || '';

export const mapKoodistoOptions = (koodit: Koodi[], language) =>
    koodit.map((koodi: Koodi) => ({
        value: `${dropKoodiVersionSuffix(koodi.uri)}#${koodi.versio}`,
        label: mapLocalizedKoodiToLang(language, 'nimi', koodi),
    }));

export const mapValuesToSelect = (KoodiUriValues: string[], selectOptions) =>
    KoodiUriValues.map((koodiUriWithVersion: string) =>
        selectOptions.find((koodiSelectOption) => koodiUriWithVersion === koodiSelectOption.value)
    );
