import { Koodi, LocalDate, Nimi, UiOrganisaationNimetNimi, Ryhma, Yhteystiedot, Language } from '../types/types';
import moment from 'moment';
import { APIEndpontDate } from '../types/apiTypes';

moment.locale('fi');

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
    const nowTime = moment();
    const alkuPvmInFuture = (n: UiOrganisaationNimetNimi) => {
        return moment(n.alkuPvm, 'D.M.YYYY').isAfter(nowTime);
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
                return moment(a.alkuPvm, 'D.M.YYYY').isAfter(moment(b.alkuPvm, 'D.M.YYYY')) ? -1 : 1;
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

const makeDate = (date: Date | string | undefined, format: string[] | string | undefined) => {
    if (date) {
        return moment(date, format);
    }
    return moment();
};

export const getUiDateStr = (
    dateStr?: Date | string,
    format: string[] | string | undefined = ['D-M-YYYY', 'YYYY-M-D', 'YYYY-D-M', 'M-D-YYYY'],
    long = false
): LocalDate => {
    const dateWithoutFormat = makeDate(dateStr, format);
    return dateWithoutFormat.isValid()
        ? (dateWithoutFormat.format(`D.M.yyyy${long ? ' HH:mm:ss' : ''}`) as LocalDate)
        : '';
};

export const formatUiDateStrToApi = (date?: Date | string): APIEndpontDate => {
    const dateWithoutFormat = makeDate(date, 'DD.MM.YYYY');
    return dateWithoutFormat.isValid() ? (dateWithoutFormat.format('yyyy-MM-DD') as APIEndpontDate) : '';
};
