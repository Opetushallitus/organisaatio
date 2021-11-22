import {
    checkHasSomeValueByKieli,
    dropKoodiVersionSuffix,
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
});
