export const dropKoodiVersionSuffix = (koodi: string) => {
    const hasVersioningHashtag = koodi.search('#');
    if (hasVersioningHashtag !== -1) {
        return koodi.replace(/#.+$/,'');
    }
    return koodi;
};

export const mapLocalizedKoodiToLang = (lang: string, property: string, value: any) =>
    value[property][lang] || value[property].fi || value[property].sv || value[property].en || '';
