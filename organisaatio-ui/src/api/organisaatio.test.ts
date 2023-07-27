import moment from 'moment';
import { Koodisto, LocalDate, Perustiedot, UiOrganisaatioBase, Yhteystiedot } from '../types/types';

import {
    APIEndpontDate,
    ApiOrganisaatio,
    ApiYhteystiedot,
    NewApiOrganisaatio,
    YhteystiedotOsoite,
} from '../types/apiTypes';
import {
    checkAndMapValuesToYhteystiedot,
    getApiOsoite,
    getApiYhteystieto,
    mapApiYhteystiedotToUi,
    mapApiYhteysTietoArvotToUi,
    mapUiOrganisaatioToApiToSave,
    mapUiOrganisaatioToApiToUpdate,
    mapUiYhteystiedotToApi,
} from './organisaatio';
import { KOSKIPOSTI_BASE, ROOT_OID } from '../contexts/constants';
import { ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA } from './koodisto';

const kieli = 'kieli_fi#1';

const postinumerotKoodisto: Partial<Koodisto> = {
    uri2Nimi: (uri) => {
        if (uri === 'postinumeroUri_00530') return '00530';
        return '';
    },
    arvo2Uri: (uri) => 'postinumeroUri_00530',
    uri2Arvo: (uri) => (uri ? '00530' : ''),
};

const Uiyesterday = moment(new Date(Date.now() - 86400000)).format('D.M.yyyy') as LocalDate;
const Apiyesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0] as APIEndpontDate;

const oldApiyhteystiedot = [
    {
        kieli,
        www: 'www.opetushallitus.fi',
    },
];

const uiYhteystiedot: Yhteystiedot = {
    en: {
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
    fi: {
        email: 'arpa@kuutio.fi',
        kayntiOsoite: '',
        kayntiOsoitePostiNro: '',
        postiOsoite: 'testiosoite',
        postiOsoitePostiNro: '00530',
        puhelinnumero: '',
        www: '',
        postiOsoiteToimipaikka: 'HELSINKI',
        kayntiOsoiteToimipaikka: '',
    },
    sv: {
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
        email: 'arpa@kuutio.fi',
    },
    {
        kieli,
        osoiteTyyppi: 'posti',
        osoite: 'testiosoite',
        postinumeroUri: 'postinumeroUri_00530',
        postitoimipaikka: 'HELSINKI',
    },
    {
        tyyppi: 'puhelin',
        kieli: 'kieli_sv#1',
        numero: '12345',
    },
];

const apinimet = [{ nimi: { fi: 'vanhanimi' }, alkuPvm: Apiyesterday, version: 0 }]; // yesterday

const apiOrganisaatio: ApiOrganisaatio = {
    alkuPvm: '2000-10-10',
    kieletUris: ['oppilaitoksenopetuskieli_1#1'],
    lakkautusPvm: '',
    kotipaikkaUri: 'kunta_1',
    muutKotipaikatUris: ['kunta_2#1'],
    maaUri: 'maa_1',
    nimet: apinimet,
    nimi: { fi: 'vanhanimi' },
    lyhytNimi: { fi: 'vanhanimi' },
    oid: '1.2.1',
    parentOid: '123.321',
    parentOidPath: '123.321,1.2.1',
    status: 'AKTIIVINEN',
    tyypit: [ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA],
    yhteystiedot: [...apiYhteystiedot],
    oppilaitosTyyppiUri: 'oppilaitostyyppi_11#1',
    oppilaitosKoodi: '',
    muutOppilaitosTyyppiUris: ['oppilaitostyyppi_11#1'],
    vuosiluokat: [],
    yhteystietoArvos: [],
    ytunnus: undefined,
    piilotettu: undefined,
    maskingActive: false,
};

const newApiOrganisaatio: NewApiOrganisaatio = {
    alkuPvm: '2000-10-10',
    lakkautusPvm: '',
    kieletUris: ['oppilaitoksenopetuskieli_1#1'],
    kotipaikkaUri: 'kunta_1',
    muutKotipaikatUris: ['kunta_2#1'],
    maaUri: 'maa_1',
    nimet: [{ nimi: { fi: 'uusinimi' }, alkuPvm: '2000-10-10', version: 0 }],
    nimi: { fi: 'uusinimi' },
    lyhytNimi: { fi: 'uusinimi' },
    parentOid: '123.321',
    tyypit: [ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA],
    yhteystiedot: [
        ...apiYhteystiedot,
        {
            kieli: 'kieli_fi#1',
            osoite: 'testiosoite',
            osoiteTyyppi: 'kaynti',
            postinumeroUri: 'postinumeroUri_00530',
            postitoimipaikka: 'HELSINKI',
        },
    ],
    oppilaitosTyyppiUri: 'oppilaitostyyppi_11#1',
    oppilaitosKoodi: '',
    muutOppilaitosTyyppiUris: ['oppilaitostyyppi_11#1'],
    vuosiluokat: [],
};

const kayntiosoite = {
    kieli,
    osoiteTyyppi: 'kaynti',
    osoite: 'testiosoite',
    postinumeroUri: 'postinumeroUri_00530',
    postitoimipaikka: 'HELSINKI',
};

const uiBaseTiedot: UiOrganisaatioBase = {
    apiOrganisaatio: {} as ApiOrganisaatio,
    oid: '1.2.1',
    parentOid: '123.321',
    nimet: [{ nimi: { fi: 'vanhanimi' }, alkuPvm: Uiyesterday, version: 0 }],
    parentOidPath: '123.321,1.2.1',
    apiYhteystiedot: oldApiyhteystiedot,
    currentNimi: { nimi: { fi: 'vanhanimi' }, alkuPvm: Uiyesterday, version: 0 },
    status: 'AKTIIVINEN',
    nimi: { fi: 'uusinimi' },
    maskingActive: false,
};

const uiPerustiedot: Perustiedot = {
    alkuPvm: '10.10.2000',
    kielet: [{ label: 'suomi', value: 'oppilaitoksenopetuskieli_1', arvo: '1', versio: 1, isDisabled: false }],
    kotipaikka: { label: 'Helsinki', value: 'kunta_1', arvo: '1', versio: 1, isDisabled: false },
    maa: { label: 'Suomi', value: 'maa_1', arvo: '1', versio: 1, isDisabled: false },
    muutKotipaikat: [{ label: 'muutKotipaikat', value: 'kunta_2', arvo: '2', versio: 1, isDisabled: false }],
    organisaatioTyypit: [ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA],
    oppilaitosTyyppiUri: {
        label: 'Peruskoulut',
        value: 'oppilaitostyyppi_11',
        arvo: '11',
        versio: 1,
        isDisabled: false,
    },
    oppilaitosKoodi: '',
    muutOppilaitosTyyppiUris: [
        { label: 'Peruskoulut', value: 'oppilaitostyyppi_11', arvo: '11', versio: 1, isDisabled: false },
    ],
    vuosiluokat: [],
};
describe('mapApiYhteysTietoArvotToUi', () => {
    it('Maps api yhteystietoarvot to Api format', () => {
        const expected = { koskiposti: { fi: 'fi', sv: 'sv', en: 'en' } };
        expect(
            mapApiYhteysTietoArvotToUi([
                {
                    //KOSKI sahkoposti
                    'YhteystietoArvo.arvoText': 'fi',
                    'YhteystietoArvo.kieli': 'kieli_fi#1',
                    ...KOSKIPOSTI_BASE,
                },
                {
                    //KOSKI sahkoposti
                    'YhteystietoArvo.arvoText': 'sv',
                    'YhteystietoArvo.kieli': 'kieli_sv#1',
                    ...KOSKIPOSTI_BASE,
                },
                {
                    //KOSKI sahkoposti
                    'YhteystietoArvo.arvoText': 'en',
                    'YhteystietoArvo.kieli': 'kieli_en#12',
                    ...KOSKIPOSTI_BASE,
                },
                {
                    //KOSKI sahkoposti
                    ...KOSKIPOSTI_BASE,
                    'YhteystietoArvo.arvoText': 'foo',
                    'YhteystietoArvo.kieli': 'kieli_en#12',
                    'YhteystietojenTyyppi.oid': '1.2.246.562.5.shouldignore',
                    'YhteystietoElementti.oid': '1.2.246.562.5.57850489428',
                },
            ])
        ).toEqual(expected);
    });
});
describe('mapUiYhteystiedotToApi', () => {
    it('Maps api yhteystiedot to Api array format ([yhteystieto, ...]) and removes empty attributes', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot];
        expect(
            mapUiYhteystiedotToApi({
                postinumerotKoodisto: postinumerotKoodisto as Koodisto,
                apiYhteystiedot: [...oldApiyhteystiedot],
                uiYhteystiedot: {
                    ...uiYhteystiedot,
                    osoitteetOnEri: true,
                },
            })
        ).toEqual(expect.arrayContaining(expected));
    });

    it('creates kayntiOsoite from postiOsoite when osoitteetOnEri is false', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot, kayntiosoite];
        expect(
            mapUiYhteystiedotToApi({
                postinumerotKoodisto: postinumerotKoodisto as Koodisto,
                apiYhteystiedot: [...oldApiyhteystiedot],
                uiYhteystiedot: { ...uiYhteystiedot },
            })
        ).toEqual(expect.arrayContaining(expected));
    });

    it('does not create kayntiosoite if osoitteetOnEri is true and osoite or postinumero is missing for kayntiosoita for a language', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot];
        const yhteystiedot = {
            ...uiYhteystiedot,
            sv: { ...uiYhteystiedot.sv, kayntiOsoite: '', kayntiOsoitePostiNro: '' },
            osoitteetOnEri: true,
        };
        expect(
            mapUiYhteystiedotToApi({
                postinumerotKoodisto: postinumerotKoodisto as Koodisto,
                apiYhteystiedot: [...oldApiyhteystiedot],
                uiYhteystiedot: yhteystiedot,
            })
        ).toEqual(expect.arrayContaining(expected));
    });
});

describe('mapApiYhteystiedotToUi', () => {
    it('Maps api yhteystiedot to ui object format ({ lang: { ...values },...}) and generates empty attributes', () => {
        expect(mapApiYhteystiedotToUi(postinumerotKoodisto as Koodisto, [...apiYhteystiedot])).toEqual({
            ...uiYhteystiedot,
            osoitteetOnEri: true,
        });
    });
});

describe('mapUiOrganisaatioToApiToUpdate', () => {
    const YhteystiedotsortCb = (a, b) =>
        a.hasOwnProperty('tyyppi')
            ? -1
            : b.hasOwnProperty('tyyppi')
            ? 1
            : 0 || a.hasOwnProperty('www')
            ? -1
            : b.hasOwnProperty('www')
            ? 1
            : 0 || a.hasOwnProperty('email')
            ? -1
            : b.hasOwnProperty('email')
            ? 1
            : 0 || a.kieli.localeCompare(b.kieli);
    it('Maps Ui organisaatio to api for update', () => {
        const expectedYhteystiedot = [...apiYhteystiedot, ...oldApiyhteystiedot, kayntiosoite].sort(YhteystiedotsortCb);
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToUpdate(
            { ...apiOrganisaatio } as ApiOrganisaatio,
            postinumerotKoodisto as Koodisto,
            uiBaseTiedot,
            uiYhteystiedot,
            uiPerustiedot,
            {}
        );
        mappedApiOrganisaatio.yhteystiedot = mappedApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb);
        expect(mappedApiOrganisaatio).toEqual({
            ...apiOrganisaatio,
            yhteystiedot: expectedYhteystiedot,
        });
    });
});

describe('mapUiOrganisaatioToApiToSave', () => {
    it('Maps Ui organisaatio to api for Save in correct format', () => {
        const YhteystiedotsortCb = (a, b) =>
            a.hasOwnProperty('tyyppi')
                ? -1
                : b.hasOwnProperty('tyyppi')
                ? 1
                : 0 || a.hasOwnProperty('www')
                ? -1
                : b.hasOwnProperty('www')
                ? 1
                : 0 || a.hasOwnProperty('email')
                ? -1
                : b.hasOwnProperty('email')
                ? 1
                : 0 || a.kieli.localeCompare(b.kieli);
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToSave(
            postinumerotKoodisto as Koodisto,
            uiYhteystiedot,
            { ...uiPerustiedot, nimi: { fi: 'uusinimi' } },
            '123.321'
        );
        mappedApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb);
        expect(mappedApiOrganisaatio).toEqual({
            ...newApiOrganisaatio,
            yhteystiedot: newApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb),
        });
    });
    it('Assigns Parent Oid to root if none is provided', () => {
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToSave(
            postinumerotKoodisto as Koodisto,
            uiYhteystiedot,
            uiPerustiedot
        );
        expect(mappedApiOrganisaatio.parentOid).toEqual(ROOT_OID);
    });
});

describe('checkAndMapValuesToYhteystiedot', () => {
    const yhteystiedot = [...apiYhteystiedot];
    yhteystiedot.unshift({ kieli: 'kieli_fi#1', tyyppi: 'puhelin', numero: '12345', isNew: true });
    const faultyYhteystiedot = [
        { kieli: 'kieli_fi#1', osoiteTyyppi: '', isNew: true },
        { kieli: 'kieli_fi#1', email: '', isNew: true },
        { kieli: 'kieli_fi#1', email: '', isNew: true },
    ];
    const yhteystiedotWithFaulty = yhteystiedot.concat(faultyYhteystiedot as ApiYhteystiedot[]);
    const mappedYhteystiedot = checkAndMapValuesToYhteystiedot(yhteystiedotWithFaulty);
    it('Removes yhteystiedot with empty properties when new', () => {
        expect(mappedYhteystiedot.length).toEqual(yhteystiedot.length);
    });
    it('Assigns new objects without isNew Property', () => {
        expect(mappedYhteystiedot[0]).not.toHaveProperty('isNew');
    });
});

describe('getYhteystieto', () => {
    const yhteystieto = { kieli, www: 'foo' };

    it('Finds correct element', () => {
        expect(getApiYhteystieto([yhteystieto], kieli, 'www')).toEqual({ ...yhteystieto });
    });

    it('Creates correct element on demand', () => {
        expect(getApiYhteystieto([], kieli, 'www')).toEqual({ ...yhteystieto, www: '', isNew: true });
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
        expect(getApiOsoite([yhteystieto], kieli, osoiteTyyppi)).toEqual(yhteystieto);
    });

    it('Creates correct element on demand', () => {
        expect(getApiOsoite([], kieli, osoiteTyyppi)).toEqual({
            isNew: true,
            ...yhteystieto,
            osoite: '',
            postinumeroUri: '',
            postitoimipaikka: '',
        });
    });
});
