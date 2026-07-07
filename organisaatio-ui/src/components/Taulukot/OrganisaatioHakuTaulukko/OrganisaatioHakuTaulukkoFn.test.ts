import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { containingSomeValueFilter, expandData, includeVakaToimijatFilter } from './OrganisaatioHakuTaulukkoFn';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import { Row } from '@tanstack/react-table';

const runContainingSomeValueFilter = (rows: Row<OrganisaatioHakuOrganisaatio>[], id: string, filterValue: string[]) =>
    rows.filter((row) => containingSomeValueFilter(row, id, filterValue, () => undefined));

const runIncludeVakaToimijatFilter = (rows: Row<OrganisaatioHakuOrganisaatio>[], id: string, filterValue: boolean) =>
    rows.filter((row) => includeVakaToimijatFilter(row, id, filterValue, () => undefined));

describe('OrganisaatioHakuTaulukko', () => {
    describe('expandData', () => {
        it('empty array should return empty object', () => {
            assert.deepStrictEqual(expandData([]), {});
        });
        it('single item, no childs, should result in one key', () => {
            assert.deepStrictEqual(
                expandData([{ subRows: [] as OrganisaatioHakuOrganisaatio[] } as OrganisaatioHakuOrganisaatio]),
                {
                    '0': true,
                }
            );
        });
        it('single item, one childs, should result in two keys', () => {
            assert.deepStrictEqual(
                expandData([
                    {
                        subRows: [{ subRows: [] as OrganisaatioHakuOrganisaatio[] }] as OrganisaatioHakuOrganisaatio[],
                    } as OrganisaatioHakuOrganisaatio,
                ]),
                {
                    '0': true,
                    '0.0': true,
                }
            );
        });
        it('single item, > 10 childs, should result in no keys', () => {
            assert.deepStrictEqual(
                expandData([
                    {
                        subRows: [
                            ...Array.from(Array(11).keys()).map(() => {
                                return { subRows: [] as OrganisaatioHakuOrganisaatio[] };
                            }),
                        ] as OrganisaatioHakuOrganisaatio[],
                    } as OrganisaatioHakuOrganisaatio,
                ]),
                {}
            );
        });
        it('two items, first > 10 childs, should result in one keys', () => {
            assert.deepStrictEqual(
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
                ]),
                { '1': true }
            );
        });
    });
    describe('containingSomeValueFilter', () => {
        const rows1 = [
            {
                getValue: () => ['1'],
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
            {
                getValue: () => ['2'],
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
            {
                getValue: () => ['2'],
            } as Partial<Row<OrganisaatioHakuOrganisaatio>>,
        ] as Row<OrganisaatioHakuOrganisaatio>[];
        const id = 'organisaatiotyypit';

        const tests = [
            {
                message: 'Filters correctly based on filter',
                rows: rows1,
                id: id,
                filter: ['2'],
                expected: [rows1[1], rows1[2]],
            },
            {
                message: 'Filters correctly based on multiple filters',
                rows: rows1,
                id: id,
                filter: ['2', '1'],
                expected: rows1,
            },
            {
                message: 'Passes all on empty filter',
                rows: rows1,
                id: id,
                filter: [],
                expected: rows1,
            },
            {
                message: 'Does not fail if id does not match object prop',
                rows: rows1,
                id: 'testi',
                filter: [],
                expected: rows1,
            },
        ];
        tests.forEach(({ message, rows, id, filter, expected }) => {
            it(message, () => {
                assert.deepStrictEqual(runContainingSomeValueFilter(rows, id, filter), expected);
            });
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

        it('Passes all rows when include vaka toimijat is set to true', () => {
            assert.deepStrictEqual(runIncludeVakaToimijatFilter(rows, id, true), [...rows]);
        });

        it('Filters out organisaatiotyyppi_07 and organisaatiotyyppi_08 if include vaka toimijat is set to false', () => {
            assert.deepStrictEqual(runIncludeVakaToimijatFilter(rows, id, false), [rows[1]]);
        });
    });
});
