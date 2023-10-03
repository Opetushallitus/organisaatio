import { containingSomeValueFilter, expandData, includeVakaToimijatFilter } from './OrganisaatioHakuTaulukko';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import { Row } from 'react-table';

describe('OrganisaatioHakuTaulukko', () => {
    describe('expandData', () => {
        it('empty array should return empty object', () => {
            expect(expandData([])).toEqual({});
        });
        it('single item, no childs, should result in one key', () => {
            expect(
                expandData([{ subRows: [] as OrganisaatioHakuOrganisaatio[] } as OrganisaatioHakuOrganisaatio])
            ).toEqual({
                '0': true,
            });
        });
        it('single item, one childs, should result in two keys', () => {
            expect(
                expandData([
                    {
                        subRows: [{ subRows: [] as OrganisaatioHakuOrganisaatio[] }] as OrganisaatioHakuOrganisaatio[],
                    } as OrganisaatioHakuOrganisaatio,
                ])
            ).toEqual({
                '0': true,
                '0.0': true,
            });
        });
        it('single item, > 10 childs, should result in no keys', () => {
            expect(
                expandData([
                    {
                        subRows: [
                            ...Array.from(Array(11).keys()).map(() => {
                                return { subRows: [] as OrganisaatioHakuOrganisaatio[] };
                            }),
                        ] as OrganisaatioHakuOrganisaatio[],
                    } as OrganisaatioHakuOrganisaatio,
                ])
            ).toEqual({});
        });
        it('two items, first > 10 childs, should result in one keys', () => {
            expect(
                expandData([
                    {
                        subRows: [
                            ...Array.from(Array(11).keys()).map(() => {
                                return { subRows: [] as OrganisaatioHakuOrganisaatio[] };
                            }),
                        ] as OrganisaatioHakuOrganisaatio[],
                    } as OrganisaatioHakuOrganisaatio,
                    {
                        subRows: [] as OrganisaatioHakuOrganisaatio[],
                    } as OrganisaatioHakuOrganisaatio,
                ])
            ).toEqual({ '1': true });
        });
    });
    describe('containingSomeValueFilter', () => {
        const rows1 = [
            {
                values: {
                    organisaatiotyypit: ['1'],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
            {
                values: {
                    organisaatiotyypit: ['2'],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
            {
                values: {
                    organisaatiotyypit: ['2'],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
        ] as Row<OrganisaatioHakuOrganisaatio>[];
        const id = 'organisaatiotyypit';
        test.each([
            ['Filters correctly based on filter', rows1, id, ['2'], [rows1[1], rows1[2]]],
            ['Filters correctly based on multiple filters', rows1, id, ['2', '1'], rows1],
            ['Passes all on empty filter', rows1, id, [], rows1],
            ['Does not fail if id does not match object prop', rows1, 'testi', [], rows1],
        ])('%s', (_, rows: Row<OrganisaatioHakuOrganisaatio>[], id: string, filter: string[], expected) => {
            expect(containingSomeValueFilter(rows, id, filter)).toStrictEqual(expected);
        });
    });
    describe('includeVakaToimijatFilter', () => {
        const rows = [
            {
                original: {
                    organisaatiotyypit: ['organisaatiotyyppi_07'],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
            {
                original: {
                    organisaatiotyypit: ['organisaatiotyyppi_02'],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
            {
                original: {
                    organisaatiotyypit: ['organisaatiotyyppi_08'],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
        ] as Row<OrganisaatioHakuOrganisaatio>[];
        const id = 'showVakaToimijat';
        test.each([
            ['Passes all rows when include vaka toimijat is set to true', rows, id, true, [...rows]],
            [
                'Filters out organisaatiotyyppi_07 and organisaatiotyyppi_08 if include vaka toimijat is set to false',
                rows,
                id,
                false,
                [rows[1]],
            ],
        ])('%s', (_, rows: Row<OrganisaatioHakuOrganisaatio>[], id: string, filter: boolean, expected) => {
            expect(includeVakaToimijatFilter(rows, id, filter)).toStrictEqual(expected);
        });
    });
});
