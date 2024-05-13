import moment from 'moment';
import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { Koodisto, LocalDate, Perustiedot, Yhteystiedot } from '../types/types';
import {
    APIEndpontDate,
    ApiOrganisaatio,
    ApiYhteystiedot,
    NewApiOrganisaatio,
    YhteystiedotOsoite,
    YhteystiedotEmail,
    YhteystiedotPhone,
    YhteystiedotWww,
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
} from './organisaatioMappers';
import { KOSKIPOSTI_BASE, ROOT_OID } from '../contexts/constants';
import { ORGANIAATIOTYYPPI_KOULUTUSTOIMIJA } from './koodisto';

const kieli = 'kieli_fi#1';

const postinumerotKoodisto: Partial<Koodisto> = {
    uri2Nimi: (uri) => {
        if (uri === 'postinumeroUri_00530') return '00530';
        return '';
    },
    arvo2Uri: () => 'postinumeroUri_00530',
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

const kayntiosoite: ApiYhteystiedot = {
    kieli,
    osoiteTyyppi: 'kaynti',
    osoite: 'testiosoite',
    postinumeroUri: 'postinumeroUri_00530',
    postitoimipaikka: 'HELSINKI',
};

const uiBaseTiedot = {
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
        assert.deepStrictEqual(
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
            ]),
            expected
        );
    });
});

function isEmail(a: ApiYhteystiedot): a is YhteystiedotEmail {
    return !!(a as YhteystiedotEmail).email;
}

function isPhone(a: ApiYhteystiedot): a is YhteystiedotPhone {
    return (a as YhteystiedotPhone).tyyppi === 'puhelin';
}

function isWww(a: ApiYhteystiedot): a is YhteystiedotWww {
    return !!(a as YhteystiedotWww).www;
}

function isOsoite(a: ApiYhteystiedot): a is YhteystiedotOsoite {
    return !!(a as YhteystiedotOsoite).osoite;
}

const assertDeepStrictEquals = (actual: ApiYhteystiedot[], expected: ApiYhteystiedot[]) => {
    assert.deepStrictEqual(actual.find(isEmail), expected.find(isEmail));
    assert.deepStrictEqual(actual.find(isPhone), expected.find(isPhone));
    assert.deepStrictEqual(actual.find(isWww), expected.find(isWww));
    assert.deepStrictEqual(actual.find(isOsoite), expected.find(isOsoite));
};

describe('mapUiYhteystiedotToApi', () => {
    it('Maps api yhteystiedot to Api array format ([yhteystieto, ...]) and removes empty attributes', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot];
        assertDeepStrictEquals(
            mapUiYhteystiedotToApi({
                postinumerotKoodisto: postinumerotKoodisto as Koodisto,
                apiYhteystiedot: [...oldApiyhteystiedot],
                uiYhteystiedot: {
                    ...uiYhteystiedot,
                    fi: { ...uiYhteystiedot.fi, www: 'www.opetushallitus.fi' },
                    osoitteetOnEri: true,
                },
            }),
            expected
        );
    });

    it('creates kayntiOsoite from postiOsoite when osoitteetOnEri is false', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot, kayntiosoite];
        assertDeepStrictEquals(
            mapUiYhteystiedotToApi({
                postinumerotKoodisto: postinumerotKoodisto as Koodisto,
                apiYhteystiedot: [...oldApiyhteystiedot],
                uiYhteystiedot: { ...uiYhteystiedot, fi: { ...uiYhteystiedot.fi, www: 'www.opetushallitus.fi' } },
            }),
            expected
        );
    });

    it('does not create kayntiosoite if osoitteetOnEri is true and osoite or postinumero is missing for kayntiosoita for a language', () => {
        const expected = [...apiYhteystiedot, ...oldApiyhteystiedot];
        const yhteystiedot = {
            ...uiYhteystiedot,
            fi: { ...uiYhteystiedot.fi, www: 'www.opetushallitus.fi' },
            sv: { ...uiYhteystiedot.sv, kayntiOsoite: '', kayntiOsoitePostiNro: '' },
            osoitteetOnEri: true,
        };
        assertDeepStrictEquals(
            mapUiYhteystiedotToApi({
                postinumerotKoodisto: postinumerotKoodisto as Koodisto,
                apiYhteystiedot: [...oldApiyhteystiedot],
                uiYhteystiedot: yhteystiedot,
            }),
            expected
        );
    });
});

describe('mapApiYhteystiedotToUi', () => {
    it('Maps api yhteystiedot to ui object format ({ lang: { ...values },...}) and generates empty attributes', () => {
        assert.deepStrictEqual(mapApiYhteystiedotToUi(postinumerotKoodisto as Koodisto, [...apiYhteystiedot]), {
            ...uiYhteystiedot,
            osoitteetOnEri: true,
        });
    });
});

describe('mapUiOrganisaatioToApiToUpdate', () => {
    const YhteystiedotsortCb = (a: ApiYhteystiedot, b: ApiYhteystiedot) =>
        Object.prototype.hasOwnProperty.call(a, 'tyyppi')
            ? -1
            : Object.prototype.hasOwnProperty.call(b, 'tyyppi')
            ? 1
            : 0 || Object.prototype.hasOwnProperty.call(a, 'www')
            ? -1
            : Object.prototype.hasOwnProperty.call(b, 'www')
            ? 1
            : 0 || Object.prototype.hasOwnProperty.call(a, 'email')
            ? -1
            : Object.prototype.hasOwnProperty.call(b, 'email')
            ? 1
            : 0 || a.kieli.localeCompare(b.kieli);
    it('Maps Ui organisaatio to api for update', () => {
        const expectedYhteystiedot = [...apiYhteystiedot, ...oldApiyhteystiedot, kayntiosoite].sort(YhteystiedotsortCb);
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToUpdate(
            { ...apiOrganisaatio } as ApiOrganisaatio,
            postinumerotKoodisto as Koodisto,
            uiBaseTiedot,
            { ...uiYhteystiedot, fi: { ...uiYhteystiedot.fi, www: 'www.opetushallitus.fi' } },
            uiPerustiedot,
            {}
        );
        mappedApiOrganisaatio.yhteystiedot = mappedApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb);
        assert.deepStrictEqual(mappedApiOrganisaatio, {
            ...apiOrganisaatio,
            virastoTunnus: undefined,
            yhteystiedot: expectedYhteystiedot,
        });
    });
});

describe('mapUiOrganisaatioToApiToSave', () => {
    it('Maps Ui organisaatio to api for Save in correct format', () => {
        const YhteystiedotsortCb = (a: ApiYhteystiedot, b: ApiYhteystiedot) =>
            Object.prototype.hasOwnProperty.call(a, 'tyyppi')
                ? -1
                : Object.prototype.hasOwnProperty.call(b, 'tyyppi')
                ? 1
                : 0 || Object.prototype.hasOwnProperty.call(a, 'www')
                ? -1
                : Object.prototype.hasOwnProperty.call(b, 'www')
                ? 1
                : 0 || Object.prototype.hasOwnProperty.call(a, 'email')
                ? -1
                : Object.prototype.hasOwnProperty.call(b, 'email')
                ? 1
                : 0 || a.kieli.localeCompare(b.kieli);
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToSave(
            postinumerotKoodisto as Koodisto,
            uiYhteystiedot,
            { ...uiPerustiedot, nimi: { fi: 'uusinimi' } },
            '123.321'
        );
        mappedApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb);
        assert.deepStrictEqual(mappedApiOrganisaatio, {
            ...newApiOrganisaatio,
            virastoTunnus: undefined,
            ytunnus: undefined,
            yhteystiedot: newApiOrganisaatio.yhteystiedot.sort(YhteystiedotsortCb),
        });
    });
    it('Assigns Parent Oid to root if none is provided', () => {
        const mappedApiOrganisaatio = mapUiOrganisaatioToApiToSave(
            postinumerotKoodisto as Koodisto,
            uiYhteystiedot,
            uiPerustiedot
        );
        assert.strictEqual(mappedApiOrganisaatio.parentOid, ROOT_OID);
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
        assert.strictEqual(mappedYhteystiedot.length, yhteystiedot.length);
    });
    it('Assigns new objects without isNew Property', () => {
        assert.strictEqual(mappedYhteystiedot[0].isNew, undefined);
    });
});

describe('getYhteystieto', () => {
    const yhteystieto = { kieli, www: 'foo' };

    it('Finds correct element', () => {
        assert.deepStrictEqual(getApiYhteystieto([yhteystieto], kieli, 'www'), { ...yhteystieto });
    });

    it('Creates correct element on demand', () => {
        assert.deepStrictEqual(getApiYhteystieto([], kieli, 'www'), { ...yhteystieto, www: '', isNew: true });
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
        assert.deepStrictEqual(getApiOsoite([yhteystieto], kieli, osoiteTyyppi), yhteystieto);
    });

    it('Creates correct element on demand', () => {
        assert.deepStrictEqual(getApiOsoite([], kieli, osoiteTyyppi), {
            isNew: true,
            ...yhteystieto,
            osoite: '',
            postinumeroUri: '',
            postitoimipaikka: '',
        });
    });
});
