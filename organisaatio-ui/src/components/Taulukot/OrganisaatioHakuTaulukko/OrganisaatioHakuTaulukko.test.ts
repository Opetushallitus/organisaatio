import { expandData } from './OrganisaatioHakuTaulukko';
import { ApiOrganisaatio } from '../../../types/apiTypes';

describe('OrganisaatioHakuTaulukko', () => {
    describe('expandData', () => {
        it('empty array should return empty object', () => {
            expect(expandData([])).toEqual({});
        });
        it('single item, no childs, should result in one key', () => {
            expect(expandData([{ subRows: [] as ApiOrganisaatio[] } as ApiOrganisaatio])).toEqual({
                '0': true,
            });
        });
        it('single item, one childs, should result in two keys', () => {
            expect(
                expandData([
                    {
                        subRows: [{ subRows: [] as ApiOrganisaatio[] }] as ApiOrganisaatio[],
                    } as ApiOrganisaatio,
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
                                return { subRows: [] as ApiOrganisaatio[] };
                            }),
                        ] as ApiOrganisaatio[],
                    } as ApiOrganisaatio,
                ])
            ).toEqual({});
        });
        it('two items, first > 10 childs, should result in one keys', () => {
            expect(
                expandData([
                    {
                        subRows: [
                            ...Array.from(Array(11).keys()).map((a) => {
                                return { subRows: [] as ApiOrganisaatio[] };
                            }),
                        ] as ApiOrganisaatio[],
                    } as ApiOrganisaatio,
                    {
                        subRows: [] as ApiOrganisaatio[],
                    } as ApiOrganisaatio,
                ])
            ).toEqual({ '1': true });
        });
    });
});
