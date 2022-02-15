import * as React from 'react';
import { useEffect, useMemo, useState } from 'react';
import styles from './OrganisaatioHakuTaulukko.module.css';
import { Cell, Column, HeaderGroup, Row, useExpanded, useFilters, usePagination, useTable } from 'react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import IconWrapper from '../../IconWapper/IconWrapper';
import { Hakufiltterit } from './Hakufiltterit';
import Loading from '../../Loading/Loading';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import { Link } from 'react-router-dom';
import { TarkastusLippu } from '../../TarkistusLippu/TarkastusLippu';
import { localFiltersAtom } from '../../../contexts/SearchFiltersContext';
import { useAtom } from 'jotai';
import { casMeAtom } from '../../../api/kayttooikeus';
import { languageAtom } from '../../../api/lokalisaatio';
import { kuntaKoodistoAtom, organisaatioTyypitKoodistoAtom } from '../../../api/koodisto';
import { SelectOptionType } from '../../../types/types';

const MAX_EXPAND_ROWS = 10;

const mapPaginationSelectors = (index) => {
    if (index < 3) return [0, 5];
    return [index - 2, index + 3];
};

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
        return rowValue.some((r) => filterValue.includes(r));
    });
};

export const includeVakaToimijatFilter = (
    rows: Row<OrganisaatioHakuOrganisaatio>[],
    id: string,
    filterValue: boolean
): Row<OrganisaatioHakuOrganisaatio>[] =>
    rows.filter(
        (row) =>
            filterValue ||
            !row.values.organisaatiotyypit.some((r) => ['organisaatiotyyppi_07', 'organisaatiotyyppi_08'].includes(r))
    );

const ExpandIcon = ({ isExpanded }) => {
    if (isExpanded) return <IconWrapper icon={chevronDown} />;
    return <IconWrapper icon={chevronRight} />;
};

const mapOptionsToValues = (options: SelectOptionType[]) => options.map((o) => o.value);

export default function OrganisaatioHakuTaulukko() {
    const [i18n] = useAtom(languageAtom);
    const [casMe] = useAtom(casMeAtom);
    const crudOids = useMemo(() => casMe.getCRUDOids(), [casMe]);
    const [organisaatiot, setOrganisaatiot] = useState<OrganisaatioHakuOrganisaatio[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [kuntaKoodisto] = useAtom(kuntaKoodistoAtom);
    const [organisaatioTyypitKoodisto] = useAtom(organisaatioTyypitKoodistoAtom);

    const containingFilter = React.useCallback(containingSomeValueFilter, []);
    const vakatoimijatFilter = React.useCallback(includeVakaToimijatFilter, []);

    const columns = React.useMemo<Column<OrganisaatioHakuOrganisaatio>[]>(
        () => [
            {
                id: 'expander',
                collapse: true,
                Cell: ({ row }) =>
                    row.canExpand ? (
                        <span
                            className={styles.Expander}
                            {...row.getToggleRowExpandedProps({
                                style: {
                                    paddingLeft: `${row.depth + 1}rem`,
                                },
                            })}
                        >
                            <ExpandIcon isExpanded={row.isExpanded} />
                        </span>
                    ) : null,
            },
            {
                Header: i18n.translate('TAULUKKO_NIMI'),
                id: 'lyhytNimi',
                Cell: ({ row }) => {
                    return (
                        <Link to={`/lomake/${row.original.oid}`}>
                            {i18n.translateNimi(row.original.lyhytNimi)}
                            {row.original?.status !== 'AKTIIVINEN' && ` (${i18n.translate('LABEL_PASSIIVINEN')})`}
                        </Link>
                    );
                },
            },
            {
                Header: i18n.translate('TAULUKKO_KUNTA'),
                accessor: (values) => {
                    const nimi = kuntaKoodisto.uri2Nimi(values.kotipaikkaUri);
                    return nimi || '';
                },
            },
            {
                Header: i18n.translate('TAULUKKO_TYYPPI'),
                id: 'organisaatiotyypit',
                accessor: (values) => values.allOrganisaatioTyypit,
                Cell: ({ row }) => (
                    <span>
                        {row.original.organisaatiotyypit
                            .map((ot) => organisaatioTyypitKoodisto.uri2Nimi(ot))
                            .join(', ')}
                    </span>
                ),
                filter: (rows, id, filterValue) => containingFilter(rows, id, mapOptionsToValues(filterValue)),
            },
            {
                Header: i18n.translate('TAULUKKO_TUNNISTE'),
                accessor: (values) => {
                    return values.ytunnus || values.oppilaitosKoodi;
                },
            },
            {
                Header: i18n.translate('LABEL_OID'),
                accessor: 'oid',
            },
            {
                Header: i18n.translate('TAULUKKO_TARKASTUS'),
                id: 'tarkistus',
                Cell: ({ row }) => {
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
                Header: 'allOids',
                id: 'allOids',
                accessor: (values) => values.allOids,
                hidden: true,
                filter: containingFilter,
            },
            {
                Header: 'oppilaitostyyppi',
                id: 'oppilaitostyyppi',
                accessor: (values) => values.allOppilaitosTyypit,
                hidden: true,
                filter: (rows: Row<OrganisaatioHakuOrganisaatio>[], id: string, filterValue: SelectOptionType[]) =>
                    containingFilter(rows, id, mapOptionsToValues(filterValue)),
            },
            {
                Header: 'showVakaToimijat',
                id: 'showVakaToimijat',
                accessor: (values) => values.organisaatiotyypit,
                hidden: true,
                filter: vakatoimijatFilter,
            },
        ],
        [i18n, kuntaKoodisto, organisaatioTyypitKoodisto, containingFilter, vakatoimijatFilter]
    );
    const sortOrganisations = useMemo(
        () => (nodes: OrganisaatioHakuOrganisaatio[]): OrganisaatioHakuOrganisaatio[] => {
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

    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        prepareRow,
        page, // Instead of using 'rows', we'll use page,
        // which has only the rows for the active page
        // The rest of these things are super handy, too ;)
        canPreviousPage,
        canNextPage,
        pageOptions,
        gotoPage,
        setAllFilters,
        nextPage,
        previousPage,
        setPageSize,
        state: { pageIndex, pageSize },
    } = useTable<OrganisaatioHakuOrganisaatio>(
        {
            columns,
            data,
            initialState: {
                pageIndex: 0,
                expanded: initialExpanded,
                hiddenColumns: ['allOids', 'oppilaitostyyppi', 'showVakaToimijat'],
                filters: [
                    { id: 'allOids', value: crudOids },
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
                ],
            },
            paginateExpandedRows: false,
            getSubRows: (row) => row.subRows || [], // fix suggested by to reset subro
        },
        useFilters,
        useExpanded,
        usePagination
    );
    const [{ omatOrganisaatiotSelected, organisaatioTyyppi, oppilaitosTyyppi, showVakaToimijat }] = useAtom(
        localFiltersAtom
    );
    useEffect(() => {
        setAllFilters([
            {
                id: 'allOids',
                value: omatOrganisaatiotSelected ? crudOids : [],
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
    }, [
        data,
        crudOids,
        setAllFilters,
        omatOrganisaatiotSelected,
        organisaatioTyyppi,
        oppilaitosTyyppi,
        showVakaToimijat,
    ]);
    return (
        <div>
            <Hakufiltterit setOrganisaatiot={setOrganisaatiot} setLoading={setLoading} />

            {(loading && <Loading />) || (
                <div className={styles.TaulukkoContainer}>
                    <table {...getTableProps()} className={styles.Taulukko}>
                        <thead>
                            {headerGroups.map((headerGroup: HeaderGroup<OrganisaatioHakuOrganisaatio>) => (
                                <tr {...headerGroup.getHeaderGroupProps()}>
                                    {headerGroup.headers.map((column) => (
                                        <th
                                            {...column.getHeaderProps({
                                                className: (column as HeaderGroup<OrganisaatioHakuOrganisaatio> & {
                                                    collapse: boolean;
                                                }).collapse
                                                    ? styles.collapse
                                                    : '',
                                            })}
                                            style={{
                                                textAlign: 'left',
                                                borderBottom: '1px solid rgba(151,151,151,0.5)',
                                            }}
                                        >
                                            {column.render('Header')}
                                        </th>
                                    ))}
                                </tr>
                            ))}
                        </thead>
                        <tbody {...getTableBodyProps()}>
                            {page.map((row, index) => {
                                prepareRow(row);
                                return (
                                    <tr {...row.getRowProps()}>
                                        {row.cells.map((cell: Cell<OrganisaatioHakuOrganisaatio>) => {
                                            return (
                                                <td
                                                    {...cell.getCellProps({
                                                        className: (cell.row as Row<OrganisaatioHakuOrganisaatio> & {
                                                            collapse: boolean;
                                                        }).collapse
                                                            ? styles.collapse
                                                            : '',
                                                    })}
                                                    style={{
                                                        background: index % 2 === 0 ? '#F5F5F5' : '#FFFFFF',
                                                    }}
                                                >
                                                    {cell.render('Cell')}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>

                    <div className={styles.PaginationContainer}>
                        <div className={styles.PaginationSivunvaihto}>
                            <Button
                                variant={'text'}
                                color={'secondary'}
                                onClick={previousPage}
                                disabled={!canPreviousPage}
                            >
                                <IconWrapper icon={chevronLeft} />
                            </Button>
                            {pageOptions.slice(...mapPaginationSelectors(pageIndex)).map((option) => {
                                if (option === pageIndex)
                                    return (
                                        <Button key={option + 1} onClick={() => gotoPage(option)}>
                                            {option + 1}
                                        </Button>
                                    );
                                return (
                                    <Button
                                        key={option + 1}
                                        variant={'text'}
                                        color={'secondary'}
                                        onClick={() => gotoPage(option)}
                                    >
                                        {option + 1}
                                    </Button>
                                );
                            })}
                            <Button variant={'text'} color={'secondary'} onClick={nextPage} disabled={!canNextPage}>
                                <IconWrapper icon={chevronRight} />
                            </Button>
                        </div>
                        <div className={styles.PaginationYhteensa}>
                            <span>{i18n.translate('TAULUKKO_NAYTA_SIVULLA')}:</span>
                            <select
                                className={styles.NaytaSivullaSelect}
                                value={pageSize}
                                onChange={(e) => {
                                    setPageSize(Number(e.target.value));
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
            )}
        </div>
    );
}
