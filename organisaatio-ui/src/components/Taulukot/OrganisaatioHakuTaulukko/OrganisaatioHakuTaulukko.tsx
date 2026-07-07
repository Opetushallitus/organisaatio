import * as React from 'react';
import { useEffect, useMemo, useState } from 'react';
import styles from './OrganisaatioHakuTaulukko.module.css';
import {
    ColumnDef,
    ColumnFiltersState,
    ExpandedState,
    flexRender,
    getCoreRowModel,
    getExpandedRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    PaginationState,
    useReactTable,
} from '@tanstack/react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import IconWrapper from '../../IconWapper/IconWrapper';
import { Hakufiltterit } from './Hakufiltterit';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import { TarkastusLippu } from '../../TarkistusLippu/TarkastusLippu';
import { localFiltersAtom } from '../../../contexts/SearchFiltersContext';
import { useAtom } from 'jotai';
import { casMeAtom } from '../../../api/kayttooikeus';
import { languageAtom } from '../../../api/lokalisaatio';
import { kuntaKoodistoAtom, organisaatioTyypitKoodistoAtom } from '../../../api/koodisto';
import { SelectOptionType } from '../../../types/types';
import { OrganisaatioLink } from '../../OrganisaatioComponents';
import { containingSomeValueFilter, expandData, includeVakaToimijatFilter } from './OrganisaatioHakuTaulukkoFn';
import LoadingBubbles from '../../Loading/LoadingBubbles';

const mapPaginationSelectors = (index: number) => {
    if (index < 3) return [0, 5];
    return [index - 2, index + 3];
};

const ExpandIcon = ({ isExpanded }: { isExpanded: boolean }) => {
    if (isExpanded) return <IconWrapper icon={chevronDown} />;
    return <IconWrapper icon={chevronRight} />;
};

const mapOptionsToValues = (options: SelectOptionType[]) => options.map((o) => o.value);

type ColumnMeta = {
    collapse?: boolean;
};

const getColumnClassName = (columnDef: ColumnDef<OrganisaatioHakuOrganisaatio>) =>
    (columnDef.meta as ColumnMeta | undefined)?.collapse ? styles.collapse : '';

export default function OrganisaatioHakuTaulukko() {
    const [i18n] = useAtom(languageAtom);
    const [casMe] = useAtom(casMeAtom);
    const omatOrganisaatiot = useMemo(() => casMe.getOrganisationOidsWithAnyAccess(), [casMe]);
    const [organisaatiot, setOrganisaatiot] = useState<OrganisaatioHakuOrganisaatio[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([
        { id: 'allOids', value: omatOrganisaatiot },
        {
            id: 'organisaatiotyypit',
            value: [],
        },
        {
            id: 'oppilaitostyyppi',
            value: [],
        },
        {
            id: 'showVakaToimijat',
            value: false,
        },
    ]);
    const [expanded, setExpanded] = useState<ExpandedState>({});
    const [pagination, setPagination] = useState<PaginationState>({
        pageIndex: 0,
        pageSize: 10,
    });
    const [kuntaKoodisto] = useAtom(kuntaKoodistoAtom);
    const [organisaatioTyypitKoodisto] = useAtom(organisaatioTyypitKoodistoAtom);

    const containingFilter = React.useCallback(containingSomeValueFilter, []);
    const vakatoimijatFilter = React.useCallback(includeVakaToimijatFilter, []);

    const columns = React.useMemo<ColumnDef<OrganisaatioHakuOrganisaatio>[]>(
        () => [
            {
                id: 'expander',
                meta: { collapse: true },
                cell: ({ row }) =>
                    row.getCanExpand() ? (
                        <button
                            aria-label={row.getIsExpanded() ? 'Sulje' : 'Avaa'}
                            className={styles.Expander}
                            onClick={row.getToggleExpandedHandler()}
                            style={{
                                paddingLeft: `${row.depth + 1}rem`,
                            }}
                            type="button"
                        >
                            <ExpandIcon isExpanded={row.getIsExpanded()} />
                        </button>
                    ) : null,
            },
            {
                header: i18n.translate('TAULUKKO_NIMI'),
                id: 'lyhytNimi',
                cell: ({ row }) => {
                    return (
                        <OrganisaatioLink
                            oid={row.original.oid}
                            nimi={row.original.lyhytNimi}
                            status={row.original.status}
                        />
                    );
                },
            },
            {
                header: i18n.translate('TAULUKKO_KUNTA'),
                accessorFn: (values) => {
                    const nimi = kuntaKoodisto.uri2Nimi(values.kotipaikkaUri);
                    return nimi || '';
                },
            },
            {
                header: i18n.translate('TAULUKKO_TYYPPI'),
                id: 'organisaatiotyypit',
                accessorFn: (values) => values.allOrganisaatioTyypit,
                cell: ({ row }) => (
                    <span>
                        {row.original.organisaatiotyypit
                            .map((ot) => organisaatioTyypitKoodisto.uri2Nimi(ot))
                            .join(', ')}
                    </span>
                ),
                filterFn: (row, id, filterValue, addMeta) =>
                    containingFilter(row, id, mapOptionsToValues(filterValue as SelectOptionType[]), addMeta),
            },
            {
                header: i18n.translate('TAULUKKO_TUNNISTE'),
                accessorFn: (values) => {
                    return values.ytunnus || values.oppilaitosKoodi;
                },
            },
            {
                header: i18n.translate('LABEL_OID'),
                accessorKey: 'oid',
            },
            {
                header: i18n.translate('TAULUKKO_TARKASTUS'),
                id: 'tarkistus',
                cell: ({ row }) => {
                    return (
                        <TarkastusLippu
                            tarkastusPvm={row.original.tarkastusPvm}
                            lakkautusPvm={row.original.lakkautusPvm}
                            alkuPvm={row.original.alkuPvm}
                            organisaatioTyypit={row.original.organisaatiotyypit}
                        />
                    );
                },
            },
            {
                header: 'allOids',
                id: 'allOids',
                accessorFn: (values) => values.allOids,
                filterFn: containingFilter,
            },
            {
                header: 'oppilaitostyyppi',
                id: 'oppilaitostyyppi',
                accessorFn: (values) => values.allOppilaitosTyypit,
                filterFn: (row, id, filterValue, addMeta) =>
                    containingFilter(row, id, mapOptionsToValues(filterValue as SelectOptionType[]), addMeta),
            },
            {
                header: 'showVakaToimijat',
                id: 'showVakaToimijat',
                accessorFn: (values) => values.organisaatiotyypit,
                filterFn: vakatoimijatFilter,
            },
        ],
        [i18n, kuntaKoodisto, organisaatioTyypitKoodisto, containingFilter, vakatoimijatFilter]
    );
    const sortOrganisations = useMemo(
        () =>
            (nodes: OrganisaatioHakuOrganisaatio[]): OrganisaatioHakuOrganisaatio[] => {
                nodes.sort((a, b) => i18n.translateNimi(a.nimi).localeCompare(i18n.translateNimi(b.nimi)));
                nodes.forEach(function (node) {
                    if (node?.subRows && node.subRows.length > 0) {
                        sortOrganisations(node.subRows);
                    }
                });
                return nodes;
            },
        [i18n]
    );
    const data = React.useMemo(() => sortOrganisations(organisaatiot), [organisaatiot, sortOrganisations]);
    const initialExpanded = React.useMemo(() => expandData(data), [data]);

    useEffect(() => {
        setExpanded(initialExpanded);
    }, [initialExpanded]);

    const table = useReactTable({
        columns,
        data,
        state: {
            columnFilters,
            columnVisibility: {
                allOids: false,
                oppilaitostyyppi: false,
                showVakaToimijat: false,
            },
            expanded,
            pagination,
        },
        onColumnFiltersChange: setColumnFilters,
        onExpandedChange: setExpanded,
        onPaginationChange: setPagination,
        defaultColumn: {
            cell: ({ getValue }) => getValue() as React.ReactNode,
        },
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getExpandedRowModel: getExpandedRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSubRows: (row) => row.subRows || [],
        paginateExpandedRows: false,
    });
    const pageIndex = pagination.pageIndex;
    const pageSize = pagination.pageSize;
    const pageOptions = Array.from({ length: table.getPageCount() }, (_, index) => index);
    const [{ omatOrganisaatiotSelected, organisaatioTyyppi, oppilaitosTyyppi, showVakaToimijat }] =
        useAtom(localFiltersAtom);
    useEffect(() => {
        setColumnFilters([
            {
                id: 'allOids',
                value: omatOrganisaatiotSelected ? omatOrganisaatiot : [],
            },
            {
                id: 'organisaatiotyypit',
                value: organisaatioTyyppi,
            },
            {
                id: 'oppilaitostyyppi',
                value: oppilaitosTyyppi,
            },
            {
                id: 'showVakaToimijat',
                value: showVakaToimijat,
            },
        ]);
    }, [data, omatOrganisaatiot, omatOrganisaatiotSelected, organisaatioTyyppi, oppilaitosTyyppi, showVakaToimijat]);
    return (
        <div>
            <Hakufiltterit setOrganisaatiot={setOrganisaatiot} setLoading={setLoading} />
            <div className={styles.TaulukkoContainer}>
                <table className={styles.Taulukko}>
                    <thead>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <tr key={headerGroup.id}>
                                {headerGroup.headers.map((header) => (
                                    <th
                                        className={getColumnClassName(header.column.columnDef)}
                                        key={header.id}
                                        style={{
                                            textAlign: 'left',
                                            borderBottom: '1px solid rgba(151,151,151,0.5)',
                                        }}
                                    >
                                        {header.isPlaceholder
                                            ? null
                                            : flexRender(header.column.columnDef.header, header.getContext())}
                                    </th>
                                ))}
                            </tr>
                        ))}
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr>
                                <td colSpan={table.getVisibleLeafColumns().length} style={{ textAlign: 'center' }}>
                                    <LoadingBubbles />
                                </td>
                            </tr>
                        ) : (
                            table.getRowModel().rows.map((row, index) => {
                                return (
                                    <tr key={row.id}>
                                        {row.getVisibleCells().map((cell) => {
                                            return (
                                                <td
                                                    className={getColumnClassName(cell.column.columnDef)}
                                                    key={cell.id}
                                                    style={{
                                                        background: index % 2 === 0 ? '#F5F5F5' : '#FFFFFF',
                                                    }}
                                                >
                                                    {cell.column.columnDef.cell
                                                        ? flexRender(cell.column.columnDef.cell, cell.getContext())
                                                        : (cell.getValue() as React.ReactNode)}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                );
                            })
                        )}
                    </tbody>
                </table>

                <div className={styles.PaginationContainer}>
                    <div className={styles.PaginationSivunvaihto}>
                        <Button
                            variant={'text'}
                            color={'secondary'}
                            onClick={() => table.previousPage()}
                            disabled={!table.getCanPreviousPage()}
                        >
                            <IconWrapper icon={chevronLeft} />
                        </Button>
                        {pageOptions.slice(...mapPaginationSelectors(pageIndex)).map((option) => {
                            if (option === pageIndex)
                                return (
                                    <Button key={option + 1} onClick={() => table.setPageIndex(option)}>
                                        {option + 1}
                                    </Button>
                                );
                            return (
                                <Button
                                    key={option + 1}
                                    variant={'text'}
                                    color={'secondary'}
                                    onClick={() => table.setPageIndex(option)}
                                >
                                    {option + 1}
                                </Button>
                            );
                        })}
                        <Button
                            variant={'text'}
                            color={'secondary'}
                            onClick={() => table.nextPage()}
                            disabled={!table.getCanNextPage()}
                        >
                            <IconWrapper icon={chevronRight} />
                        </Button>
                    </div>
                    <div className={styles.PaginationYhteensa}>
                        <span>{i18n.translate('TAULUKKO_NAYTA_SIVULLA')}:</span>
                        <select
                            className={styles.NaytaSivullaSelect}
                            value={pageSize}
                            onChange={(e) => {
                                table.setPageSize(Number(e.target.value));
                            }}
                        >
                            {[10, 20, 30, 40, 50].map((pageSizeOption) => (
                                <option key={pageSizeOption} value={pageSizeOption}>
                                    {pageSizeOption}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>
        </div>
    );
}
