import moment from 'moment';
import {
    checkHasSomeValueByKieli,
    dropKoodiVersionSuffix,
    getUiDateStr,
    formatUiDateStrToApi,
    mapLocalizedKoodiToLang,
    mapVisibleKieletFromOpetuskielet,
} from './mappers';
import { YhteystiedotBase } from '../types/types';

describe('mappers', () => {
    const koodiWithVersion = 'kieli_fi#1';
    const koodiWithoutVersion = 'kieli_fi';

    const esimerkkiMonikielinenObjekti = {
        uri: 'uri',
        arvo: 'arvo',
        nimi: {
            fi: 'suominimi',
            sv: 'ruotsinimi',
            en: 'enkkunimi',
        },
        versio: 1,
        kuvaus: {
            sv: 'ruotsikuvaus',
        },
        emptyprop: {},
    };
    describe('dropKoodiVersionSuffix', () => {
        it('Drops koodi version suffix #', () => {
            expect(dropKoodiVersionSuffix(koodiWithVersion)).toBe(koodiWithoutVersion);
        });

        it('Works with koodis without version suffix', () => {
            expect(dropKoodiVersionSuffix(koodiWithoutVersion)).toBe('kieli_fi');
        });
    });
    describe('mapLocalizedKoodiToLang', () => {
        it('Maps koodi to possible browser language en', () => {
            const lang = 'en';
            expect(mapLocalizedKoodiToLang(lang, 'nimi', esimerkkiMonikielinenObjekti)).toBe('enkkunimi');
        });

        it('Maps koodi to fi lang if language is not supplied and is available', () => {
            expect(mapLocalizedKoodiToLang('', 'nimi', esimerkkiMonikielinenObjekti)).toBe('suominimi');
        });

        it('Maps koodi to first available lang sv, if language is not supplied', () => {
            expect(mapLocalizedKoodiToLang('', 'kuvaus', esimerkkiMonikielinenObjekti)).toBe('ruotsikuvaus');
        });
        it('Maps koodi to empty string if there are no language props', () => {
            expect(mapLocalizedKoodiToLang('', 'emptyprop', esimerkkiMonikielinenObjekti)).toBe('');
        });
    });

    describe('mapVisibleKieletFromOpetuskielet', () => {
        test.each([
            ['Handles invalid input to fi', undefined, ['fi']],
            ['Handles empty input to fi', [], ['fi']],
            ['Map correctly', ['suomi'], ['fi']],
            ['Strips duplicates', ['suomi', 'suomi'], ['fi']],
            ['Multiple values', ['suomi/ruotsi'], ['fi', 'sv']],
            ['Sorts correctly', ['muu', 'ruotsi', 'suomi'], ['fi', 'sv', 'en']],
            ['Handles unanticipated language', ['muu', 'ruotsi', 'suomi', 'swahili'], ['fi', 'sv', 'en']],
        ])('%s', (_, input, expected) => expect(mapVisibleKieletFromOpetuskielet(input)).toStrictEqual(expected));
    });

    describe('checkHasSomeValueByKieli', () => {
        test.each([
            [
                {
                    email: '',
                    kayntiOsoitePostiNro: '',
                    kayntiOsoiteToimipaikka: '',
                    puhelinnumero: '',
                    www: '',
                    postiOsoite: 'asetettu',
                    postiOsoitePostiNro: '',
                    postiOsoiteToimipaikka: '',
                    kayntiOsoite: '',
                },
                true,
            ],
            [
                {
                    email: '',
                    kayntiOsoitePostiNro: '',
                    kayntiOsoiteToimipaikka: '',
                    puhelinnumero: '',
                    www: '',
                    postiOsoite: '',
                    postiOsoitePostiNro: '',
                    postiOsoiteToimipaikka: '',
                    kayntiOsoite: '',
                },
                false,
            ],
        ])('Returns true if some field of yhteystiedot object is set and false if not set', (values, expected) => {
            expect(checkHasSomeValueByKieli(values as YhteystiedotBase)).toEqual(expected);
        });
    });

    describe('getUiDateStr', () => {
        const expectedTodayDate = moment().format('D.M.yyyy');
        const expectedTestDayDate = moment('2020-1-22').format('D.M.yyyy');
        const expectedTestDayLongDate = moment('2020-10-22').format('D.M.yyyy HH:mm:ss');
        test.each([
            ['Handles invalid input to empty string', 'dsdsds', undefined, false, ''],
            ['Handles empty input to current day in correct format', undefined, undefined, false, expectedTodayDate],
            [
                'Handles date input to correct day in correct format',
                new Date('2020-1-22'),
                undefined,
                false,
                expectedTestDayDate,
            ],
            [
                'Handles valid text input to correct day in correct format',
                '01-22-2020',
                undefined,
                false,
                expectedTestDayDate,
            ],
            [
                'Handles valid "DD-MM-YYYY" input to correct day in correct format',
                '1-22-2020',
                undefined,
                false,
                expectedTestDayDate,
            ],
            [
                'Handles valid "MM-DD-YYYY" input to correct day in correct format',
                '1-22-2020',
                undefined,
                false,
                expectedTestDayDate,
            ],
            [
                'Handles valid "YYYY-MM-DD" input to correct day in correct format',
                '2020-1-22',
                undefined,
                false,
                expectedTestDayDate,
            ],
            [
                'Handles valid "YYYY-DD-MM" input to correct day in correct format',
                '2020-22-1',
                undefined,
                false,
                expectedTestDayDate,
            ],
            ['Handles valid text whith long formatting', '10-22-2020', undefined, true, expectedTestDayLongDate],
        ])('%s', (_, input, format, long, expected) =>
            expect(getUiDateStr(input, format, long)).toStrictEqual(expected)
        );
    });
    describe('formatUiDateStrToApi', () => {
        const expectedTodayDate = moment().format('yyyy-MM-DD');
        const expectedTestDayDate = moment('09-22-2020').format('yyyy-MM-DD');
        test.each([
            ['Handles invalid input to empty string', 'dsdsds', ''],
            ['Handles empty input to current day in correct format', undefined, expectedTodayDate],
            ['Handles date input to correct day in correct format', new Date('9.22.2020'), expectedTestDayDate],
            ['Handles valid text input to correct day in correct format', '22.9.2020', expectedTestDayDate],
        ])('%s', (_, input, expected) => expect(formatUiDateStrToApi(input)).toStrictEqual(expected));
    });
});
