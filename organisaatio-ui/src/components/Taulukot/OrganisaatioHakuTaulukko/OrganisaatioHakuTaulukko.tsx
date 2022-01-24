import * as React from 'react';
import { useContext, useEffect, useMemo, useState } from 'react';
import styles from './OrganisaatioHakuTaulukko.module.css';
import { Cell, Column, HeaderGroup, Row, useExpanded, useFilters, usePagination, useTable } from 'react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { ApiOrganisaatio } from '../../../types/apiTypes';
import { languageAtom } from '../../../contexts/LanguageContext';
import IconWrapper from '../../IconWapper/IconWrapper';
import { Hakufiltterit } from './Hakufiltterit';
import Loading from '../../Loading/Loading';
import chevronDown from '@iconify/icons-fa-solid/chevron-down';
import { Link } from 'react-router-dom';
import { KoodistoContext } from '../../../contexts/KoodistoContext';
import { casMeAtom } from '../../../contexts/CasMeContext';
import { TarkastusLippu } from '../../TarkistusLippu/TarkastusLippu';
import { localFiltersAtom } from '../../../contexts/SearchFiltersContext';
import { useAtom } from 'jotai';

const MAX_EXPAND_ROWS = 10;
const mapPaginationSelectors = (index) => {
    if (index < 3) return [0, 5];
    return [index - 2, index + 3];
};

export const expandData = (data: ApiOrganisaatio[], parent?: string, initial = {}) => {
    return data.reduce((p, c, i) => {
        const me = parent ? `${parent}.${i}` : `${i}`;
        if (!!c.subRows && c.subRows.length <= MAX_EXPAND_ROWS) {
            p[me] = true;
            expandData(c.subRows, me, p);
        }
        return p;
    }, initial);
};
export const allOids = (data: ApiOrganisaatio[]) => {
    return data.reduce((p: string[], c) => {
        const subs = !!c.subRows ? allOids(c.subRows) : [];
        return [...p, ...subs, c.oid];
    }, []);
};
const ExpandIcon = ({ isExpanded }) => {
    if (isExpanded) return <IconWrapper icon={chevronDown} />;
    return <IconWrapper icon={chevronRight} />;
};
export default function OrganisaatioHakuTaulukko() {
    const [i18n] = useAtom(languageAtom);
    const [casMe] = useAtom(casMeAtom);
    const crudOids = useMemo(() => casMe.getCRUDOids(), [casMe]);
    const [organisaatiot, setOrganisaatiot] = useState<ApiOrganisaatio[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const { kuntaKoodisto, organisaatioTyypitKoodisto } = useContext(KoodistoContext);
    const columns = React.useMemo<Column<ApiOrganisaatio>[]>(
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
                id: 'nimi',
                accessor: (values) => {
                    return i18n.translateNimi(values.nimi);
                },
                Cell: ({ row }) => {
                    return (
                        <Link to={`/lomake/${row.original.oid}`}>
                            {i18n.translateNimi(row.original.nimi)}
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
                accessor: (values) => {
                    return values.tyypit;
                },
                Cell: ({ row }) => (
                    <span>
                        {row.original.organisaatiotyypit
                            .map((ot) => organisaatioTyypitKoodisto.uri2Nimi(ot))
                            .join(', ')}
                    </span>
                ),
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
                Header: 'containingOids',
                id: 'containingOids',
                accessor: (values) => {
                    return [...values.parentOidPath.split('/'), ...allOids([values])];
                },
                hidden: true,
                filter: (rows, id, filterValue) => {
                    if (filterValue.length === 0) return rows;
                    return rows.filter((row) => {
                        const rowValue = row.values[id];
                        return rowValue.some((r) => filterValue.includes(r));
                    });
                },
            },
        ],
        [i18n, kuntaKoodisto, organisaatioTyypitKoodisto]
    );
    const sortOrganisations = useMemo(
        () => (nodes: ApiOrganisaatio[]): ApiOrganisaatio[] => {
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
        setFilter,
        nextPage,
        previousPage,
        setPageSize,
        state: { pageIndex, pageSize },
    } = useTable<ApiOrganisaatio>(
        {
            columns,
            data,
            initialState: {
                pageIndex: 0,
                expanded: initialExpanded,
                hiddenColumns: ['containingOids'],
                filters: [{ id: 'containingOids', value: crudOids }],
            },
            paginateExpandedRows: false,
        },
        useFilters,
        useExpanded,
        usePagination
    );
    const [{ omatOrganisaatiotSelected }] = useAtom(localFiltersAtom);
    useEffect(() => {
        if (omatOrganisaatiotSelected) setFilter('containingOids', crudOids);
        else setFilter('containingOids', []);
    }, [data, crudOids, setFilter, omatOrganisaatiotSelected]);
    return (
        <div>
            <Hakufiltterit setOrganisaatiot={setOrganisaatiot} setLoading={setLoading} />

            {(loading && <Loading />) || (
                <>
                    <table {...getTableProps()} style={{ width: '100%', borderSpacing: 0 }}>
                        <thead>
                            {headerGroups.map((headerGroup: HeaderGroup<ApiOrganisaatio>) => (
                                <tr {...headerGroup.getHeaderGroupProps()}>
                                    {headerGroup.headers.map((column) => (
                                        <th
                                            {...column.getHeaderProps({
                                                className: (column as HeaderGroup<ApiOrganisaatio> & {
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
                                        {row.cells.map((cell: Cell<ApiOrganisaatio>) => {
                                            return (
                                                <td
                                                    {...cell.getCellProps({
                                                        className: (cell.row as Row<ApiOrganisaatio> & {
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
                                onClick={() => previousPage()}
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
                            <Button
                                variant={'text'}
                                color={'secondary'}
                                onClick={() => nextPage()}
                                disabled={!canNextPage}
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
                </>
            )}
        </div>
    );
}
