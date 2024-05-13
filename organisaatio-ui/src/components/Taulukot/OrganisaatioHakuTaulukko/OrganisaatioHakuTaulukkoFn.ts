import { Row } from 'react-table';

import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';

const MAX_EXPAND_ROWS = 10;

export const expandData = (data: OrganisaatioHakuOrganisaatio[], parent?: string, initial = {}) => {
    return data.reduce((p, c, i) => {
        const me = parent ? `${parent}.${i}` : `${i}`;
        if (!!c.subRows && c.subRows.length <= MAX_EXPAND_ROWS) {
            p[me] = true;
            expandData(c.subRows, me, p);
        }
        return p;
    }, initial);
};

export const containingSomeValueFilter = (
    rows: Row<OrganisaatioHakuOrganisaatio>[],
    id: string,
    filterValue: string[]
): Row<OrganisaatioHakuOrganisaatio>[] => {
    if (filterValue.length === 0) return rows;
    return rows.filter((row) => {
        const rowValue = row.values[id];
        return rowValue.some((r: string) => filterValue.includes(r));
    });
};

export const includeVakaToimijatFilter = (
    rows: Row<OrganisaatioHakuOrganisaatio>[],
    _id: string,
    filterValue: boolean
): Row<OrganisaatioHakuOrganisaatio>[] =>
    rows.filter((row) => {
        const organisaatiotyypit = row.original.organisaatiotyypit;
        return (
            filterValue ||
            !(
                organisaatiotyypit.length === 1 && // this is because some kunta type and other type orgs are also vaka toimija, and maybe we should not filter them out!
                organisaatiotyypit.some((r) => ['organisaatiotyyppi_07', 'organisaatiotyyppi_08'].includes(r))
            )
        );
    });
