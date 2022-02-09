import { enrichWithAllNestedData } from './Hakufiltterit';
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
                        } as Partial<OrganisaatioHakuOrganisaatio>,
                    ],
                } as Partial<OrganisaatioHakuOrganisaatio>,
                {
                    parentOidPath: '3',
                    oid: '3',
                    organisaatiotyypit: ['2'],
                    subRows: [],
                } as Partial<OrganisaatioHakuOrganisaatio>,
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
                                } as Partial<OrganisaatioHakuOrganisaatio>,
                            ],
                        } as Partial<OrganisaatioHakuOrganisaatio>,
                    ],
                } as Partial<OrganisaatioHakuOrganisaatio>,
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
                        } as Partial<OrganisaatioHakuOrganisaatio>,
                    ],
                } as Partial<OrganisaatioHakuOrganisaatio>,
                {
                    oid: '3',
                    parentOidPath: '3',
                    allOids: ['3'],
                    organisaatiotyypit: ['2'],
                    allOrganisaatioTyypit: ['2'],
                    allOppilaitosTyypit: [],
                    subRows: [],
                } as Partial<OrganisaatioHakuOrganisaatio>,
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
                                } as Partial<OrganisaatioHakuOrganisaatio>,
                            ],
                        },
                    ],
                } as Partial<OrganisaatioHakuOrganisaatio>,
            ] as OrganisaatioHakuOrganisaatio[];
            expect(enrichWithAllNestedData(data1)).toEqual(expected);
        });
    });
});
