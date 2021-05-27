import { dropKoodiVersionSuffix, mapLocalizedKoodiToLang } from './mappers';

describe('mappers', () => {
    const koodiWithVersion = 'kieli_fi#1';
    const koodiWithoutVersion = 'kieli_fi';

    const esimerkkiMonikielinenObjekti = {
        nimi: {
            fi: 'suominimi',
            sv: 'ruotsinimi',
            en: 'enkkunimi',
        },
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
});
