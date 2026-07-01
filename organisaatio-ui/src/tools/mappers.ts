import { Koodi, LocalDate, Nimi, UiOrganisaationNimetNimi, Ryhma, Yhteystiedot, Language } from '../types/types';
import { APIEndpontDate } from '../types/apiTypes';
import { format } from 'date-fns/format';
import { isAfter } from 'date-fns/isAfter';
import { API_DATE_FORMAT, formatDateInput, parseDateInput, UI_DATE_FORMAT } from './dateUtils';

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

export const mapVisibleKieletFromOpetuskielet = (opetuskielet: string[] | undefined): Language[] => {
    const priority = ['fi', 'sv', 'en'];
    const mapping = {
        suomi: ['fi'],
        ruotsi: ['sv'],
        'suomi/ruotsi': ['fi', 'sv'],
        englanti: ['en'],
        muu: ['en'],
    };
    const sort = (input: Language[]): Language[] => input.sort((a, b) => priority.indexOf(a) - priority.indexOf(b));
    return sort(
        (opetuskielet?.length ? opetuskielet : ['suomi']).reduce(
            (acc, lang) => [...acc, ...(mapping[lang] || [])],
            [] as Language[]
        )
    ).filter((value, index, self) => self.indexOf(value) === index);
};

export const checkHasSomeValueByKieli = (KielisetYhteystiedot: Yhteystiedot[Language]): boolean => {
    return (
        Object.keys(KielisetYhteystiedot).filter((yhteystietokentta) => KielisetYhteystiedot[yhteystietokentta])
            .length > 0
    );
};

export const sortNimet = (
    nimet: UiOrganisaationNimetNimi[],
    nimi: Nimi
): {
    currentNimi: UiOrganisaationNimetNimi;
    pastNimet: UiOrganisaationNimetNimi[];
    futureNimet: UiOrganisaationNimetNimi[];
} => {
    const nowTime = new Date();
    const alkuPvmInFuture = (n: UiOrganisaationNimetNimi) => {
        const alkuPvm = parseDateInput(n.alkuPvm, UI_DATE_FORMAT);
        return alkuPvm ? isAfter(alkuPvm, nowTime) : false;
    };
    const [mappedFutureNimet, mappedPastNimet] = nimet
        .reduce(
            (
                [futureNimet, pastNimet]: [UiOrganisaationNimetNimi[], UiOrganisaationNimetNimi[]],
                elem
            ): [futureNimet: UiOrganisaationNimetNimi[], fail: UiOrganisaationNimetNimi[]] => {
                return alkuPvmInFuture(elem)
                    ? [[...futureNimet, elem], pastNimet]
                    : [futureNimet, [...pastNimet, elem]];
            },
            [[], []]
        )
        .map((nimetArr) =>
            [...nimetArr].sort((a, b) => {
                const alkuPvmA = parseDateInput(a.alkuPvm, UI_DATE_FORMAT);
                const alkuPvmB = parseDateInput(b.alkuPvm, UI_DATE_FORMAT);
                return alkuPvmA && alkuPvmB && isAfter(alkuPvmA, alkuPvmB) ? -1 : 1;
            })
        );
    const currentNimiIndex = mappedPastNimet.findIndex(
        (pastNimi) => JSON.stringify(pastNimi.nimi) === JSON.stringify(nimi)
    );
    if (currentNimiIndex === 0) {
        mappedPastNimet[0].isCurrentNimi = true;
    } else {
        mappedFutureNimet[0].isCurrentNimi = true;
    }
    const currentNimi =
        currentNimiIndex >= 0 ? { ...mappedPastNimet[0] } : { ...mappedFutureNimet[mappedFutureNimet.length - 1] };
    return { currentNimi, pastNimet: mappedPastNimet, futureNimet: mappedFutureNimet };
};

export const getUiDateStr = (
    dateStr?: Date | string,
    dateFormats: string[] | string | undefined = ['d-M-yyyy', 'yyyy-M-d', 'yyyy-d-M', 'M-d-yyyy'],
    long = false
): LocalDate => {
    return formatDateInput(dateStr, `${UI_DATE_FORMAT}${long ? ' HH:mm:ss' : ''}`, dateFormats, true) as LocalDate;
};

export const formatUiDateStrToApi = (date?: Date | string): APIEndpontDate => {
    const parsedDate = parseDateInput(date, UI_DATE_FORMAT, true);
    return parsedDate ? (format(parsedDate, API_DATE_FORMAT) as APIEndpontDate) : '';
};
