import { dropKoodiVersionSuffix, mapKoodistoOptions, mapLocalizedKoodiToLang, mapValuesToSelect } from './mappers';
import { Koodi } from '../types/types';

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

    describe('mapKoodistoOptions', () => {
        const koodit: Koodi[] = [
            {
                uri: 'koodi_1',
                nimi: {
                    fi: 'Koodi',
                    sv: 'Kod',
                    en: 'Code',
                },
                arvo: '1',
                versio: 2,
            },
        ];

        it('Maps koodit to options array with value and label', () => {
            const options = mapKoodistoOptions(koodit, 'fi');
            expect(options.length).toBe(1);
            expect(options[0].value).toBe('koodi_1#2');
            expect(options[0].label).toBe('Koodi');
        });
    });
    describe('mapValuesToSelect', () => {
        const kooditUrit: string[] = ['koodi_2#1'];
        const kooditSelectOptions = [
            {
                label: 'Koodi2',
                value: 'koodi_2#1',
            },
        ];

        it('Maps ', () => {
            const selectValues = mapValuesToSelect(kooditUrit, kooditSelectOptions);
            expect(selectValues.length).toBe(1);
            expect(selectValues[0].value).toBe('koodi_2#1');
            expect(selectValues[0].label).toBe('Koodi2');
        });
    });
});
