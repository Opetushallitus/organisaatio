import { Koodi, Koodisto, Perustiedot, UiOrganisaatioBase, Yhteystiedot } from '../types/types';
import { ApiOrganisaatio, ApiYhteystiedot, NewApiOrganisaatio, YhteystiedotOsoite } from '../types/apiTypes';
import {
    getApiOsoite,
    getApiYhteystieto,
    mapApiYhteystiedotToUi,
    mapUiYhteystiedotToApi,
    mapUiOrganisaatioToApiToUpdate,
    mapUiOrganisaatioToApiToSave,
    checkAndMapValuesToYhteystiedot,
} from './organisaatio';
import { ROOT_OID } from '../contexts/contexts';

const kieli = 'kieli_fi#1';

const postinumerotKoodisto: Partial<Koodisto> = {
    uri2Nimi: (uri) => {
        if (uri === 'postinumeroUri_00530') return '00530';
        return '';
    },
    arvo2Uri: (uri) => 'postinumeroUri_00530',
};

const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0];
const today = new Date().toISOString().split('T')[0];

const oldApiyhteystiedot = [
    {
        kieli,
        www: 'www.opetushallitus.fi',
    },
];

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
        postiOsoitePostiNro: '00530',
        puhelinnumero: '',
        www: '',
        postiOsoiteToimipaikka: 'HELSINKI',
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

const apinimet = [{ nimi: { fi: 'vanhanimi' }, alkuPvm: yesterday }]; // yesterday

const apiOrganisaatio: ApiOrganisaatio = {
    alkuPvm: '',
    kieletUris: [],
    kotipaikkaUri: 'kunta_1#1',
    muutKotipaikatUris: [],
    maaUri: 'maa_1#1',
    nimet: apinimet,
    nimi: { fi: 'vanhanimi' },
    oid: '1.2.1',
    parentOid: '123.321',
    parentOidPath: '123.321,1.2.1',
    status: 'AKTIIVINEN',
    tyypit: [],
    yhteystiedot: [...apiYhteystiedot],
};

const newApiOrganisaatio: NewApiOrganisaatio = {
    alkuPvm: '',
    kieletUris: [],
    kotipaikkaUri: 'kunta_1#1',
    muutKotipaikatUris: [],
    maaUri: 'maa_1#1',
    nimet: [{ nimi: { fi: 'nimenvaihto' }, alkuPvm: today }],
    nimi: { fi: 'nimenvaihto' },
    parentOid: '1.2.11.2',
    tyypit: [],
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
};

const kayntiosoite = {
    kieli,
    osoiteTyyppi: 'kaynti',
    osoite: 'testiosoite',
    postinumeroUri: 'postinumeroUri_00530',
    postitoimipaikka: 'HELSINKI',
};

const uiBaseTiedot: UiOrganisaatioBase = {
    oid: '1.2.1',
    parentOid: '123.321',
    nimet: [{ nimi: { fi: 'vanhanimi' }, alkuPvm: yesterday }],
    parentOidPath: '123.321,1.2.1',
    apiYhteystiedot: oldApiyhteystiedot,
    currentNimi: { fi: 'vanhanimi' },
    status: 'AKTIIVINEN',
};

const uiPerustiedot: Perustiedot = {
    alkuPvm: '',
    kielet: [],
    kotipaikka: { label: 'Helsinki', value: 'kunta_1#1' },
    maa: { label: 'Suomi', value: 'maa_1#1' },
    muutKotipaikat: [],
    nimi: { fi: 'uusinimi' },
    organisaatioTyypit: [],
};

describe('mapUiYhteystiedotToApi', () => {
    it('Maps api yhteystiedot to Api array format ([yhteystieto, ...]) and removes empty attributes', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot];
        expect(
            mapUiYhteystiedotToApi(postinumerotKoodisto as Koodisto, [...oldApiyhteystiedot], {
                ...uiYhteystiedot,
                osoitteetOnEri: true,
            })
        ).toEqual(expect.arrayContaining(expected));
    });

    it('creates kayntiOsoite from postiOsoite when osoitteetOnEri is false', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot, kayntiosoite];
        expect(
            mapUiYhteystiedotToApi(postinumerotKoodisto as Koodisto, [...oldApiyhteystiedot], { ...uiYhteystiedot })
        ).toEqual(expect.arrayContaining(expected));
    });
});

describe('mapApiYhteystiedotToUi', () => {
    it('Maps api yhteystiedot to ui object format ({ lang: { ...values },...}) and generates empty attributes', () => {
        expect(mapApiYhteystiedotToUi(postinumerotKoodisto as Koodisto, [...apiYhteystiedot])).toEqual({
            ...uiYhteystiedot,
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
        const expectedNimet = [...apiOrganisaatio.nimet];
        expectedNimet.push({ nimi: { fi: 'uusinimi' }, alkuPvm: today });
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToUpdate(
            postinumerotKoodisto as Koodisto,
            uiBaseTiedot,
            uiYhteystiedot,
            uiPerustiedot
        );
        mappedApiOrganisaatio.yhteystiedot = mappedApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb);
        expect(mappedApiOrganisaatio).toEqual({
            ...apiOrganisaatio,
            nimi: { fi: 'uusinimi' },
            nimet: expectedNimet,
            yhteystiedot: expectedYhteystiedot,
        });
    });

    it('Updates nimet nimi to be same as nimi if update is done on the same day', () => {
        const expectedNimet = [
            { nimi: { fi: 'vanhanimi' }, alkuPvm: yesterday },
            { nimi: { fi: 'nimenvaihto' }, alkuPvm: today },
        ];
        const { nimet, ...rest } = uiBaseTiedot;
        uiPerustiedot.nimi = { fi: 'nimenvaihto' };
        const { nimet: mappedNimet } = mapUiOrganisaatioToApiToUpdate(
            postinumerotKoodisto as Koodisto,
            { nimet, ...rest },
            uiYhteystiedot,
            uiPerustiedot
        );
        expect(mappedNimet).toEqual(expectedNimet);
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
        uiBaseTiedot.nimet = [];
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToSave(
            postinumerotKoodisto as Koodisto,
            uiYhteystiedot,
            uiPerustiedot,
            '1.2.11.2'
        );
        mappedApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb);
        expect(mappedApiOrganisaatio.nimet[0].alkuPvm).toEqual(today);
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