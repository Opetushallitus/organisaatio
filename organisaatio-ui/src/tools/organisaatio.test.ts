import { resolveOrganisaatioTyypit } from './organisaatio';
import { rakenne, ROOT_OID } from '../contexts/contexts';
import { Koodi, Koodisto } from '../types/types';
const koodisto: Partial<Koodisto> = {
    uri2SelectOption: (uri) => {
        return { label: uri, value: uri };
    },
    koodit: () =>
        [
            {
                uri: 'organisaatiotyyppi_01',
            },
            {
                uri: 'organisaatiotyyppi_02',
            },
            {
                uri: 'organisaatiotyyppi_03',
            },
            {
                uri: 'organisaatiotyyppi_04',
            },
            {
                uri: 'organisaatiotyyppi_05',
            },
            {
                uri: 'organisaatiotyyppi_06',
            },
            {
                uri: 'organisaatiotyyppi_07',
            },
            {
                uri: 'organisaatiotyyppi_08',
            },
            {
                uri: 'organisaatiotyyppi_09',
            },
        ] as Koodi[],
};

describe('resolveOrganisaatioTyypit', () => {
    it('Works for empty list', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: [],
                oid: '123',
            })
        ).toStrictEqual([]);
    });
    it('Works for combining two', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_02',
                label: 'organisaatiotyyppi_02',
            },
            {
                label: 'organisaatiotyyppi_04',
                value: 'organisaatiotyyppi_04',
            },
            {
                label: 'organisaatiotyyppi_08',
                value: 'organisaatiotyyppi_08',
            },
        ]);
    });
    it('Works when second has no children', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_02',
                label: 'organisaatiotyyppi_02',
            },
            {
                value: 'organisaatiotyyppi_04',
                label: 'organisaatiotyyppi_04',
            },
        ]);
    });
    it('Works for combining three', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07', 'organisaatiotyyppi_09'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_02',
                label: 'organisaatiotyyppi_02',
            },
            {
                value: 'organisaatiotyyppi_04',
                label: 'organisaatiotyyppi_04',
            },
            {
                value: 'organisaatiotyyppi_08',
                label: 'organisaatiotyyppi_08',
            },
        ]);
    });
    it('Works when parent is ROOT', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodisto as Koodisto, {
                organisaatioTyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: ROOT_OID,
            })
        ).toStrictEqual([
            {
                value: 'organisaatiotyyppi_01',
                label: 'organisaatiotyyppi_01',
            },
            {
                value: 'organisaatiotyyppi_05',
                label: 'organisaatiotyyppi_05',
            },
            {
                value: 'organisaatiotyyppi_06',
                label: 'organisaatiotyyppi_06',
            },
            {
                value: 'organisaatiotyyppi_07',
                label: 'organisaatiotyyppi_07',
            },
            {
                value: 'organisaatiotyyppi_09',
                label: 'organisaatiotyyppi_09',
            },
        ]);
    });
});