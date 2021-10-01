import { resolveOrganisaatioTyypit } from './organisaatio';
import { rakenne, ROOT_OID } from '../contexts/contexts';
import { Koodi } from '../types/types';
const koodistoTyypit = [
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
];

describe('resolveOrganisaatioTyypit', () => {
    it('Works for empty list', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodistoTyypit as Koodi[], {
                tyypit: [],
                oid: '123',
            })
        ).toStrictEqual([]);
    });
    it('Works for combining two', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodistoTyypit as Koodi[], {
                tyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                uri: 'organisaatiotyyppi_02',
            },
            {
                uri: 'organisaatiotyyppi_04',
            },
            {
                uri: 'organisaatiotyyppi_08',
            },
        ]);
    });
    it('Works when second has no children', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodistoTyypit as Koodi[], {
                tyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                uri: 'organisaatiotyyppi_02',
            },
            {
                uri: 'organisaatiotyyppi_04',
            },
        ]);
    });
    it('Works for combining three', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodistoTyypit as Koodi[], {
                tyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_07', 'organisaatiotyyppi_09'],
                oid: '123',
            })
        ).toStrictEqual([
            {
                uri: 'organisaatiotyyppi_02',
            },
            {
                uri: 'organisaatiotyyppi_04',
            },
            {
                uri: 'organisaatiotyyppi_08',
            },
        ]);
    });
    it('Works when parent is ROOT', () => {
        expect(
            resolveOrganisaatioTyypit(rakenne, koodistoTyypit as Koodi[], {
                tyypit: ['organisaatiotyyppi_01', 'organisaatiotyyppi_09'],
                oid: ROOT_OID,
            })
        ).toStrictEqual([
            {
                uri: 'organisaatiotyyppi_01',
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
                uri: 'organisaatiotyyppi_09',
            },
        ]);
    });
});
