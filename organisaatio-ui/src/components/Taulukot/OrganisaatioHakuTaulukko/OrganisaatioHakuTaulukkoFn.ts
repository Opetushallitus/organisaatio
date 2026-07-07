import { FilterFn } from '@tanstack/react-table';

import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';

const MAX_EXPAND_ROWS = 10;

export const expandData = (
    data: OrganisaatioHakuOrganisaatio[],
    parent?: string,
    initial: Record<string, boolean> = {}
) => {
    return data.reduce((p, c, i) => {
        const me = parent ? `${parent}.${i}` : `${i}`;
        if (!!c.subRows && c.subRows.length <= MAX_EXPAND_ROWS) {
            p[me] = true;
            expandData(c.subRows, me, p);
        }
        return p;
    }, initial);
};

export const containingSomeValueFilter: FilterFn<OrganisaatioHakuOrganisaatio> = (row, id, filterValue) => {
    const values = filterValue as string[];
    if (values.length === 0) return true;
    const rowValue = row.getValue<string[]>(id);
    return Array.isArray(rowValue) && rowValue.some((r: string) => values.includes(r));
};

export const includeVakaToimijatFilter: FilterFn<OrganisaatioHakuOrganisaatio> = (row, _id, filterValue) => {
    const organisaatiotyypit = row.original.organisaatiotyypit;
    return (
        filterValue ||
        !(
            organisaatiotyypit.length === 1 && // this is because some kunta type and other type orgs are also vaka toimija, and maybe we should not filter them out!
            organisaatiotyypit.some((r) => ['organisaatiotyyppi_07', 'organisaatiotyyppi_08'].includes(r))
        )
    );
};
