import { dropKoodiVersionSuffix, mapLocalizedKoodiToLang, mapVisibleKieletFromOpetuskielet } from './mappers';

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
        it('maps suomi only opetuskieli to [fi] visible based on label', () => {
            const opetuskeleletOptions = [{ label: 'suomi', value: 'testi' }];
            expect(mapVisibleKieletFromOpetuskielet(opetuskeleletOptions)).toStrictEqual(['fi']);
        });
        it('maps ruotsi only opetuskieli to [sv] visible based on label', () => {
            const opetuskeleletOptions = [{ label: 'ruotsi', value: 'testi' }];
            expect(mapVisibleKieletFromOpetuskielet(opetuskeleletOptions)).toStrictEqual(['sv']);
        });
        it('maps muu only opetuskieli to [en] visible based on label', () => {
            const opetuskeleletOptions = [{ label: 'muu', value: 'testi' }];
            expect(mapVisibleKieletFromOpetuskielet(opetuskeleletOptions)).toStrictEqual(['en']);
        });

        it('maps suomi/ruotsi only opetuskieli to [fi, sv] visible based on label', () => {
            const opetuskeleletOptions = [{ label: 'suomi/ruotsi', value: 'testi' }];
            expect(mapVisibleKieletFromOpetuskielet(opetuskeleletOptions)).toStrictEqual(['fi', 'sv']);
        });
        it('maps suomi/ruotsi and muu opetuskieli to [fi, sv, en] visible based on label', () => {
            const opetuskeleletOptions = [
                { label: 'suomi/ruotsi', value: 'testi' },
                { label: 'muu', value: 'muutesti' },
            ];
            expect(mapVisibleKieletFromOpetuskielet(opetuskeleletOptions)).toStrictEqual(['fi', 'sv', 'en']);
        });
        it('maps none opetuskieli to [fi] visible based on label', () => {
            const opetuskeleletOptions = [];
            expect(mapVisibleKieletFromOpetuskielet(opetuskeleletOptions)).toStrictEqual(['fi']);
        });
    });
});
