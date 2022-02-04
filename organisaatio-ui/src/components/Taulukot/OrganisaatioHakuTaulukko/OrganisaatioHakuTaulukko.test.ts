import { containingSomeValueFilter, expandData } from './OrganisaatioHakuTaulukko';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';

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
                            ...Array.from(Array(11).keys()).map((a) => {
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
                            ...Array.from(Array(11).keys()).map((a) => {
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
                },
            },
            {
                values: {
                    organisaatiotyypit: ['2'],
                },
            },
            {
                values: {
                    organisaatiotyypit: ['2'],
                },
            },
        ];
        const id = ['organisaatiotyypit'];
        test.each([
            ['Filters correctly based on filter', rows1, id, ['2'], [rows1[1], rows1[2]]],
            ['Filters correctly based on multiple filters', rows1, id, ['2', '1'], rows1],
            ['Passes all on empty filter', rows1, id, [], rows1],
            ['Does not fail if id does not match object prop', rows1, 'testi', [], rows1],
        ])('%s', (_, rows, id, filter, expected) => {
            // @ts-ignore:next-line
            expect(containingSomeValueFilter(rows, id, filter)).toStrictEqual(expected);
        });
    });
});
