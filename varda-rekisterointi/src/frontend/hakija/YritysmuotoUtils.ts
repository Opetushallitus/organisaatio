import { Koodi, Language } from "../types";

const yritysmuotoPriorityList = [
    'Aatteellinen yhdistys',
    'Avoin yhtiö',
    'Ei yritysmuotoa',
    'Ev.lut.kirkko',
    'Julkinen osakeyhtiö',
    'Kommandiittiyhtiö',
    'Muu julkisoikeudellinen oikeushenkilö',
    'Muu säätiö',
    'Muu taloudellinen yhdistys',
    'Muu verotuksen yksikkö',
    'Muu yhdistys',
    'Muu yhteisvast.pidätysvelvollinen',
    'Muu yhtiö',
    'Muut oikeushenkilöt',
    'Ortodoksinen kirkko',
    'Osakeyhtiö',
    'Osuuskunta',
    'Rekisteröity uskonnollinen yhdyskunta',
    'Sivuliike',
    'Säätiö',
    'Taloudellinen yhdistys',
    'Yksityinen elinkeinonharjoittaja'
];

export function yritysmuotoSortFnByLanguage(language: Language) {
    return function(item1: {value: string, label: string}, item2: {value: string, label: string}) {
        const item1Priority = yritysmuotoPriorityList.includes(item1.value);
        const item2Priority = yritysmuotoPriorityList.includes(item2.value);
        if (item1Priority && !item2Priority) {
            return -1;
        }
        if (!item1Priority && item2Priority) {
            return 1;
        }
        return item1.label.localeCompare(item2.label, language);
    }
}

export function yritysmuotoValueFn(koodi: Koodi) {
    return koodi.nimi.fi || koodi.uri
}
