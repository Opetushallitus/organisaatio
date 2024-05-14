import moment from 'moment';
import assert from 'assert/strict';
import { describe, it } from 'node:test';

import {
    checkHasSomeValueByKieli,
    dropKoodiVersionSuffix,
    getUiDateStr,
    formatUiDateStrToApi,
    mapLocalizedKoodiToLang,
    mapVisibleKieletFromOpetuskielet,
    sortNimet,
} from './mappers';
import { LocalDate, YhteystiedotBase } from '../types/types';

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
        tila: 'LUONNOS' as const,
    };
    describe('dropKoodiVersionSuffix', () => {
        it('Drops koodi version suffix #', () => {
            assert.strictEqual(dropKoodiVersionSuffix(koodiWithVersion), koodiWithoutVersion);
        });

        it('Works with koodis without version suffix', () => {
            assert.strictEqual(dropKoodiVersionSuffix(koodiWithoutVersion), 'kieli_fi');
        });
    });
    describe('mapLocalizedKoodiToLang', () => {
        it('Maps koodi to possible browser language en', () => {
            const lang = 'en';
            assert.strictEqual(mapLocalizedKoodiToLang(lang, 'nimi', esimerkkiMonikielinenObjekti), 'enkkunimi');
        });

        it('Maps koodi to fi lang if language is not supplied and is available', () => {
            assert.strictEqual(mapLocalizedKoodiToLang('', 'nimi', esimerkkiMonikielinenObjekti), 'suominimi');
        });

        it('Maps koodi to first available lang sv, if language is not supplied', () => {
            assert.strictEqual(mapLocalizedKoodiToLang('', 'kuvaus', esimerkkiMonikielinenObjekti), 'ruotsikuvaus');
        });
        it('Maps koodi to empty string if there are no language props', () => {
            assert.strictEqual(mapLocalizedKoodiToLang('', 'emptyprop', esimerkkiMonikielinenObjekti), '');
        });
    });

    describe('mapVisibleKieletFromOpetuskielet', () => {
        it('Handles invalid input to fi', () => {
            assert.deepStrictEqual(mapVisibleKieletFromOpetuskielet(undefined), ['fi']);
        });
        it('Handles empty input to fi', () => {
            assert.deepStrictEqual(mapVisibleKieletFromOpetuskielet([]), ['fi']);
        });
        it('Map correctly', () => {
            assert.deepStrictEqual(mapVisibleKieletFromOpetuskielet(['suomi']), ['fi']);
        });
        it('Strips duplicates', () => {
            assert.deepStrictEqual(mapVisibleKieletFromOpetuskielet(['suomi', 'suomi']), ['fi']);
        });
        it('Multiple values', () => {
            assert.deepStrictEqual(mapVisibleKieletFromOpetuskielet(['suomi/ruotsi']), ['fi', 'sv']);
        });
        it('Sorts correctly', () => {
            assert.deepStrictEqual(mapVisibleKieletFromOpetuskielet(['muu', 'ruotsi', 'suomi']), ['fi', 'sv', 'en']);
        });
    });

    describe('checkHasSomeValueByKieli', () => {
        it('Returns true if some field of yhteystiedot object is set and false if not set', () => {
            assert.strictEqual(
                checkHasSomeValueByKieli({
                    email: '',
                    kayntiOsoitePostiNro: '',
                    kayntiOsoiteToimipaikka: '',
                    puhelinnumero: '',
                    www: '',
                    postiOsoite: 'asetettu',
                    postiOsoitePostiNro: '',
                    postiOsoiteToimipaikka: '',
                    kayntiOsoite: '',
                } as YhteystiedotBase),
                true
            );
            assert.strictEqual(
                checkHasSomeValueByKieli({
                    email: '',
                    kayntiOsoitePostiNro: '',
                    kayntiOsoiteToimipaikka: '',
                    puhelinnumero: '',
                    www: '',
                    postiOsoite: '',
                    postiOsoitePostiNro: '',
                    postiOsoiteToimipaikka: '',
                    kayntiOsoite: '',
                } as YhteystiedotBase),
                false
            );
        });
    });

    describe('sortNimet', () => {
        it('Sorts nimet based on alkupvm date to past and future and setsCurrentName', () => {
            const pastNimi = {
                nimi: { fi: 'mennyt' },
                alkuPvm: moment().subtract(1, 'days').format('D.M.yyyy') as LocalDate,
                version: 0,
            };
            const futureNimi = {
                nimi: { fi: 'tuleva' },
                alkuPvm: moment().add(1, 'days').format('D.M.yyyy') as LocalDate,
                version: 0,
            };
            const expectedPastNimet = [pastNimi];
            const expectedFutureNimet = [futureNimi];
            const nimet = [pastNimi, futureNimi];
            const { currentNimi, pastNimet, futureNimet } = sortNimet(nimet, pastNimi.nimi);
            assert.deepStrictEqual(currentNimi, pastNimi);
            assert.strictEqual(pastNimet[0].isCurrentNimi, true);
            assert.deepStrictEqual(pastNimet, expectedPastNimet);
            assert.deepStrictEqual(futureNimet, expectedFutureNimet);
        });
    });

    describe('getUiDateStr', () => {
        const expectedTodayDate = moment().format('D.M.yyyy');
        const expectedTestDayDate = moment('2020-1-22').format('D.M.yyyy');
        const expectedTestDayLongDate = moment('2020-10-22').format('D.M.yyyy HH:mm:ss');
        it('Handles invalid input to empty string', () => {
            assert.strictEqual(getUiDateStr('dsdsds', undefined, false), '');
        });
        it('Handles empty input to current day in correct format', () => {
            assert.strictEqual(getUiDateStr(undefined, undefined, false), expectedTodayDate);
        });
        it('Handles date input to correct day in correct format', () => {
            assert.strictEqual(getUiDateStr(new Date('2020-1-22'), undefined, false), expectedTestDayDate);
        });
        it('Handles valid text input to correct day in correct format', () => {
            assert.strictEqual(getUiDateStr('01-22-2020', undefined, false), expectedTestDayDate);
        });
        it('Handles valid "DD-MM-YYYY" input to correct day in correct format', () => {
            assert.strictEqual(getUiDateStr('1-22-2020', undefined, false), expectedTestDayDate);
        });
        it('Handles valid "YYYY-MM-DD" input to correct day in correct format', () => {
            assert.strictEqual(getUiDateStr('2020-1-22', undefined, false), expectedTestDayDate);
        });
        it('Handles valid "YYYY-DD-MM" input to correct day in correct format', () => {
            assert.strictEqual(getUiDateStr('2020-22-1', undefined, false), expectedTestDayDate);
        });
        it('Handles valid text whith long formatting', () => {
            assert.strictEqual(getUiDateStr('10-22-2020', undefined, true), expectedTestDayLongDate);
        });
    });
    describe('formatUiDateStrToApi', () => {
        const expectedTodayDate = moment().format('yyyy-MM-DD');
        const expectedTestDayDate = moment('09-22-2020').format('yyyy-MM-DD');
        it('Handles invalid input to empty string', () => {
            assert.strictEqual(formatUiDateStrToApi('dsdsds'), '');
        });
        it('Handles empty input to current day in correct format', () => {
            assert.strictEqual(formatUiDateStrToApi(undefined), expectedTodayDate);
        });
        it('Handles date input to correct day in correct format', () => {
            assert.strictEqual(formatUiDateStrToApi(new Date('9.22.2020')), expectedTestDayDate);
        });
        it('Handles valid text input to correct day in correct format', () => {
            assert.strictEqual(formatUiDateStrToApi('22.9.2020'), expectedTestDayDate);
        });
    });
});
