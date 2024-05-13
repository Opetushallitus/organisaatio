import { dropKoodiVersionSuffix } from '../../../tools/mappers';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';

export const enrichWithAllNestedData = (
    data: OrganisaatioHakuOrganisaatio[],
    parentOrganisaatioTypes: string[] = [],
    parentOpilaitosTypes: string[] = [],
    parentOids: string[] = []
): OrganisaatioHakuOrganisaatio[] => {
    const tableDataEnricher = (organisaatioData: OrganisaatioHakuOrganisaatio[]): [string[], string[], string[]] =>
        organisaatioData
            .reduce(
                (prev: [string[], string[], string[]], c: OrganisaatioHakuOrganisaatio) => {
                    const [prevOrganisaatioTyypit, prevOppilaitosTyypit, prevOids] = prev;
                    const organisaatiotyypit = c.organisaatiotyypit || [];
                    const oppilaitostyyppi = c.oppilaitostyyppi ? [dropKoodiVersionSuffix(c.oppilaitostyyppi)] : [];
                    const [subOrganisaatioTyypit, subOppilaitosTyypit, subOids] = c.subRows
                        ? tableDataEnricher(c.subRows)
                        : [[], [], []];
                    return [
                        [
                            ...prevOrganisaatioTyypit,
                            ...subOrganisaatioTyypit,
                            ...organisaatiotyypit,
                            ...parentOrganisaatioTypes,
                        ],
                        [...prevOppilaitosTyypit, ...subOppilaitosTyypit, ...oppilaitostyyppi, ...parentOpilaitosTypes],
                        [...prevOids, ...subOids, c.oid, ...c.parentOidPath.split('/'), ...parentOids],
                    ] as [string[], string[], string[]];
                },
                [[], [], []]
            )
            .map((arr) => [...new Set(arr)]) as [string[], string[], string[]];
    return data.map((organisaatio) => {
        const [allOrganisaatioTyypit, allOppilaitosTyypit, allOids] = tableDataEnricher([organisaatio]);
        return {
            ...organisaatio,
            subRows: enrichWithAllNestedData(organisaatio.subRows, allOrganisaatioTyypit, allOppilaitosTyypit, allOids),
            allOrganisaatioTyypit,
            allOppilaitosTyypit,
            allOids,
        };
    });
};
