import { enrichWithAllOrganisaatioTyypit } from './Hakufiltterit';

describe('Hakufiltterit', () => {
    describe('enrichWithAllOrganisaatioTyypit', () => {
        it('Enriches all rows and nested rows with allOrganisaatioTyypit and allOppilaitostyypit from all levels', () => {
            const data1 = [
                {
                    organisaatiotyypit: ['1'],
                    subRows: [
                        {
                            organisaatiotyypit: ['2'],
                            oppilaitostyyppi: '5',
                            subRows: [],
                        },
                    ],
                },
                {
                    organisaatiotyypit: ['2'],
                    subRows: [],
                },
                {
                    organisaatiotyypit: ['3'],
                    subRows: [
                        {
                            organisaatiotyypit: ['3'],
                            oppilaitostyyppi: '7',
                            subRows: [
                                {
                                    organisaatiotyypit: ['2'],
                                    subRows: [],
                                    oppilaitostyyppi: '2',
                                },
                            ],
                        },
                    ],
                },
            ];
            const expected = [
                {
                    organisaatiotyypit: ['1'],
                    allOrganisaatioTyypit: ['2', '1'],
                    allOppilaitosTyypit: ['5'],
                    subRows: [
                        {
                            oppilaitostyyppi: '5',
                            organisaatiotyypit: ['2'],
                            allOrganisaatioTyypit: ['2', '1'],
                            allOppilaitosTyypit: ['5'],
                            subRows: [],
                        },
                    ],
                },
                {
                    organisaatiotyypit: ['2'],
                    allOrganisaatioTyypit: ['2'],
                    allOppilaitosTyypit: [],
                    subRows: [],
                },
                {
                    organisaatiotyypit: ['3'],
                    allOrganisaatioTyypit: ['2', '3'],
                    allOppilaitosTyypit: ['2', '7'],
                    subRows: [
                        {
                            organisaatiotyypit: ['3'],
                            allOrganisaatioTyypit: ['2', '3'],
                            allOppilaitosTyypit: ['2', '7'],
                            oppilaitostyyppi: '7',
                            subRows: [
                                {
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
            // @ts-ignore:next-line
            expect(enrichWithAllOrganisaatioTyypit(data1)).toEqual(expected);
        });
    });
});
