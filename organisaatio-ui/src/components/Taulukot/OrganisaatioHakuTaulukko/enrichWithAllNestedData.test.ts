import assert from 'assert/strict';
import { describe, it } from 'node:test';

import { enrichWithAllNestedData } from './enrichWithAllNestedData';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';

describe('Hakufiltterit', () => {
    describe('enrichWithAllOrganisaatioTyypit', () => {
        it('Enriches all rows and nested rows with allOrganisaatioTyypit and allOppilaitostyypit from all levels', () => {
            const data1 = [
                {
                    oid: '1',
                    parentOidPath: '1',
                    organisaatiotyypit: ['1'],
                    subRows: [
                        {
                            parentOidPath: '1/2',
                            oid: '2',
                            organisaatiotyypit: ['2'],
                            oppilaitostyyppi: '5',
                            subRows: [],
                        },
                    ],
                },
                {
                    parentOidPath: '3',
                    oid: '3',
                    organisaatiotyypit: ['2'],
                    subRows: [],
                },
                {
                    parentOidPath: '4',
                    organisaatiotyypit: ['3'],
                    oid: '4',
                    subRows: [
                        {
                            parentOidPath: '4/5',
                            oid: '5',
                            organisaatiotyypit: ['3'],
                            oppilaitostyyppi: '7',
                            subRows: [
                                {
                                    parentOidPath: '4/5/6',
                                    oid: '6',
                                    organisaatiotyypit: ['2'],
                                    subRows: [],
                                    oppilaitostyyppi: '2',
                                },
                            ],
                        },
                    ],
                },
            ] as OrganisaatioHakuOrganisaatio[];
            const expected = [
                {
                    organisaatiotyypit: ['1'],
                    allOrganisaatioTyypit: ['2', '1'],
                    allOppilaitosTyypit: ['5'],
                    allOids: ['2', '1'],
                    parentOidPath: '1',
                    oid: '1',
                    subRows: [
                        {
                            parentOidPath: '1/2',
                            oid: '2',
                            oppilaitostyyppi: '5',
                            organisaatiotyypit: ['2'],
                            allOrganisaatioTyypit: ['2', '1'],
                            allOppilaitosTyypit: ['5'],
                            allOids: ['2', '1'],
                            subRows: [],
                        },
                    ],
                },
                {
                    oid: '3',
                    parentOidPath: '3',
                    allOids: ['3'],
                    organisaatiotyypit: ['2'],
                    allOrganisaatioTyypit: ['2'],
                    allOppilaitosTyypit: [],
                    subRows: [],
                },
                {
                    oid: '4',
                    parentOidPath: '4',
                    allOids: ['6', '4', '5'],
                    organisaatiotyypit: ['3'],
                    allOrganisaatioTyypit: ['2', '3'],
                    allOppilaitosTyypit: ['2', '7'],
                    subRows: [
                        {
                            oid: '5',
                            parentOidPath: '4/5',
                            allOids: ['6', '4', '5'],
                            organisaatiotyypit: ['3'],
                            allOrganisaatioTyypit: ['2', '3'],
                            allOppilaitosTyypit: ['2', '7'],
                            oppilaitostyyppi: '7',
                            subRows: [
                                {
                                    oid: '6',
                                    parentOidPath: '4/5/6',
                                    allOids: ['6', '4', '5'],
                                    organisaatiotyypit: ['2'],
                                    allOrganisaatioTyypit: ['2', '3'],
                                    allOppilaitosTyypit: ['2', '7'],
                                    oppilaitostyyppi: '2',
                                    subRows: [],
                                },
                            ],
                        },
                    ],
                },
            ];
            assert.deepStrictEqual(enrichWithAllNestedData(data1), expected);
        });
    });
});
