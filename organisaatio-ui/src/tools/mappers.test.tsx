import {
    dropKoodiVersionSuffix,
    getOsoite,
    getYhteystieto,
    mapApiYhteystiedotToUi,
    mapLocalizedKoodiToLang,
    mapUiYhteystiedotToApi,
} from './mappers';
import { ApiYhteystiedot, YhteystiedotOsoite } from '../types/apiTypes';
import { Yhteystiedot } from '../types/types';

const kieli = 'kieli_fi#1';

const uiYhteystiedot: Yhteystiedot = {
    'kieli_en#1': {
        email: '',
        kayntiOsoite: '',
        kayntiOsoitePostiNro: '',
        postiOsoite: '',
        postiOsoitePostiNro: '',
        puhelinnumero: '',
        www: '',
        postiOsoiteToimipaikka: '',
        kayntiOsoiteToimipaikka: '',
    },
    'kieli_fi#1': {
        email: 'arpa@kuutio.fi',
        kayntiOsoite: '',
        kayntiOsoitePostiNro: '',
        postiOsoite: 'testiosoite',
        postiOsoitePostiNro: '22222',
        puhelinnumero: '',
        www: '',
        postiOsoiteToimipaikka: '',
        kayntiOsoiteToimipaikka: '',
    },
    'kieli_sv#1': {
        puhelinnumero: '12345',
        email: '',
        kayntiOsoite: '',
        kayntiOsoitePostiNro: '',
        postiOsoite: '',
        postiOsoitePostiNro: '',
        www: '',
        postiOsoiteToimipaikka: '',
        kayntiOsoiteToimipaikka: '',
    },
    osoitteetOnEri: false,
};

const apiYhteystiedot: ApiYhteystiedot[] = [
    {
        kieli,
        osoiteTyyppi: 'posti',
        osoite: 'testiosoite',
        postinumeroUri: '22222',
        postitoimipaikka: '',
    },
    {
        tyyppi: 'puhelin',
        kieli: 'kieli_sv#1',
        numero: '12345',
    },
    {
        kieli,
        email: 'arpa@kuutio.fi',
    },
];

const kayntiosoite = {
    kieli,
    osoiteTyyppi: 'kaynti',
    osoite: 'testiosoite',
    postinumeroUri: '22222',
    postitoimipaikka: '',
};

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

    describe('getYhteystieto', () => {
        const yhteystieto = { kieli, www: 'foo' };

        it('Finds correct element', () => {
            expect(getYhteystieto([yhteystieto], kieli, 'www')).toEqual({ ...yhteystieto });
        });

        it('Creates correct element on demand', () => {
            expect(getYhteystieto([], kieli, 'www')).toEqual({ ...yhteystieto, www: '', isNew: true });
        });
    });

    describe('getOsoite', () => {
        const osoiteTyyppi = 'posti';

        const yhteystieto = {
            kieli,
            osoiteTyyppi,
            osoite: 'testi',
        } as YhteystiedotOsoite;

        it('Finds correct element', () => {
            expect(getOsoite([yhteystieto], kieli, osoiteTyyppi)).toEqual(yhteystieto);
        });

        it('Creates correct element on demand', () => {
            expect(getOsoite([], kieli, osoiteTyyppi)).toEqual({
                isNew: true,
                ...yhteystieto,
                osoite: '',
                postinumeroUri: '',
                postitoimipaikka: '',
            });
        });
    });
    const oldApiyhteystiedot = [
        {
            kieli,
            www: 'www.opetushallitus.fi',
        },
    ];
    describe('mapUiYhteystiedotToApi', () => {
        it('Maps api yhteystiedot to Api array format ([yhteystieto, ...]) and removes empty attributes', () => {
            const expected = [...apiYhteystiedot, ...oldApiyhteystiedot];
            expect(
                mapUiYhteystiedotToApi([...oldApiyhteystiedot], { ...uiYhteystiedot, osoitteetOnEri: true })
            ).toEqual(expect.arrayContaining(expected));
        });

        it('creates kayntiOsoite from postiOsoite when osoitteetOnEri is false', () => {
            const expected = [...apiYhteystiedot, ...oldApiyhteystiedot, kayntiosoite];
            expect(mapUiYhteystiedotToApi([...oldApiyhteystiedot], { ...uiYhteystiedot })).toEqual(
                expect.arrayContaining(expected)
            );
        });
    });

    describe('mapApiYhteystiedotToUi', () => {
        it('Maps api yhteystiedot to ui object format ({ lang: { ...values },...}) and generates empty attributes', () => {
            expect(mapApiYhteystiedotToUi(apiYhteystiedot)).toEqual({ ...uiYhteystiedot });
        });
    });
});
