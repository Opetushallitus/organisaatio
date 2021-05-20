export const dropKoodiVersionSuffix = (koodi: string) => {
    const hasVersioningHashtag = koodi.search('#');
    if (hasVersioningHashtag) {
        return koodi.slice(0, hasVersioningHashtag);
    }
    return koodi;
};

export const mapLocalizedKoodiToLang = (lang: string, property: string, value: any) =>
    value[property][lang] || value[property].fi || value[property].sv || value[property].en || '';
