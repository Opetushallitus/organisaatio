import { Koodi, Language } from '../types/types';

export const kielletytYritysmuodot = [
    'yritysmuoto_41', //'Kunta',
    'yritysmuoto_42', //'Kuntayhtymä',
];

const yritysmuotoPriorityList = [
    'yritysmuoto_6', //'Aatteellinen yhdistys',
    'yritysmuoto_5', //'Avoin yhtiö',
    'yritysmuoto_0', //'Ei yritysmuotoa',
    'yritysmuoto_44', //'Ev.lut.kirkko',
    'yritysmuoto_17', //'Julkinen osakeyhtiö',
    'yritysmuoto_13', //'Kommandiittiyhtiö',
    'yritysmuoto_49', //'Muu julkisoikeudellinen oikeushenkilö',
    'yritysmuoto_39', //'Muu säätiö',
    'yritysmuoto_38', //'Muu taloudellinen yhdistys',
    'yritysmuoto_59', //'Muu verotuksen yksikkö',
    'yritysmuoto_29', //'Muu yhdistys',
    'yritysmuoto_52', //'Muu yhteisvast.pidätysvelvollinen',
    'yritysmuoto_30', //'Muu yhtiö',
    'yritysmuoto_63', //'Muut oikeushenkilöt',
    'yritysmuoto_45', //'Ortodoksinen kirkko',
    'yritysmuoto_16', //'Osakeyhtiö',
    'yritysmuoto_14', //'Osuuskunta',
    'yritysmuoto_46', //'Rekisteröity uskonnollinen yhdyskunta',
    'yritysmuoto_19', //'Sivuliike',
    'yritysmuoto_18', //'Säätiö',
    'yritysmuoto_21', //'Taloudellinen yhdistys',
    'yritysmuoto_26', //'Yksityinen elinkeinonharjoittaja'
];

export function yritysmuotoSortFnByLanguage(language: Language) {
    return function (item1: { value: string; label: string }, item2: { value: string; label: string }) {
        const item1Priority = yritysmuotoPriorityList.includes(item1.value);
        const item2Priority = yritysmuotoPriorityList.includes(item2.value);
        if (item1Priority && !item2Priority) {
            return -1;
        }
        if (!item1Priority && item2Priority) {
            return 1;
        }
        return item1.label.localeCompare(item2.label, language);
    };
}

export function yritysmuotoValueFn(koodi: Koodi) {
    return koodi.uri || (koodi.nimi && koodi.nimi.fi) || '';
}
