import * as React from 'react';
import { useState } from 'react';
import styles from './NormaaliTaulukko.module.css';
import {
    ColumnDef,
    ColumnFiltersState,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    PaginationState,
    SortingState,
    useReactTable,
} from '@tanstack/react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import searchIcon from '@iconify/icons-fa-solid/search';
import Select, { MultiValue } from 'react-select';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { Ryhma, SelectOptionType } from '../../types/types';
import IconWrapper from '../IconWapper/IconWrapper';
import { useAtom } from 'jotai';
import { languageAtom } from '../../api/lokalisaatio';
import { kayttoRyhmatKoodistoAtom, ryhmanTilaKoodistoAtom, ryhmaTyypitKoodistoAtom } from '../../api/koodisto';

const mapPaginationSelectors = (index: number) => {
    if (index < 3) return [0, 5];
    return [index - 2, index + 3];
};

export type NormaaliTaulukkoProps = {
    ryhmatData?: Ryhma[];
    ryhmatColumns?: ColumnDef<Ryhma>[];
    useHakuFiltteri?: boolean;
};

export type FiltteritProps = {
    setFilter: (columnId: string, updater: string | undefined | (string | null | undefined)[]) => void;
    setGlobalFilter: (filterValue: string | undefined) => void;
    globalFilter: string | undefined;
};

type ColumnMeta = {
    collapse?: boolean;
};

const getColumnClassName = (columnDef: ColumnDef<Ryhma>) =>
    (columnDef.meta as ColumnMeta | undefined)?.collapse ? styles.collapse : '';

export const chooseTaulukkoData = (ryhmatData: Ryhma[], ryhmatColumns: ColumnDef<Ryhma>[]) => {
    if (ryhmatData && ryhmatData.length > 0) {
        return { data: ryhmatData, columns: ryhmatColumns };
    }
    return { data: [], columns: [] };
};

export const Hakufiltterit = ({ setFilter, globalFilter, setGlobalFilter }: FiltteritProps) => {
    const [i18n] = useAtom(languageAtom);
    const [ryhmaTyypitKoodisto] = useAtom(ryhmaTyypitKoodistoAtom);
    const [kayttoRyhmatKoodisto] = useAtom(kayttoRyhmatKoodistoAtom);
    const [ryhmanTilaKoodisto] = useAtom(ryhmanTilaKoodistoAtom);
    const [tyyppiFiltteri, setTyyppiFiltteri] = useState<MultiValue<SelectOptionType>>([]);
    const [kayttoRyhmatFiltteri, setKayttoRyhmatFiltteri] = useState<MultiValue<SelectOptionType>>([]);
    const [tilaFiltteri, setTilaFiltteri] = useState<MultiValue<SelectOptionType>>([]);

    const ryhmatyypitOptions = ryhmaTyypitKoodisto.selectOptions();
    const kayttoRyhmatOptions = kayttoRyhmatKoodisto.selectOptions();
    const ryhmanTilaOptions = ryhmanTilaKoodisto.selectOptions();

    return (
        <div>
            <div className={styles.FiltteriContainer}>
                <div className={styles.FiltteriRivi}>
                    <div className={styles.FiltteriInputOsa}>
                        <Input
                            placeholder={i18n.translate('RYHMAT_HAKU_PLACEHOLDER')}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                                setGlobalFilter(e.target.value || undefined); // Set undefined to remove the filter entirely
                            }}
                            value={globalFilter ?? ''}
                            suffix={<IconWrapper color={'#999999'} icon={searchIcon} />}
                        />
                    </div>
                </div>
                <div className={styles.FiltteriRivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_RYHMAN_TYYPPI')}</label>
                        <Select
                            id={'RYHMAN_TYYPPI_SELECT'}
                            onChange={(values: MultiValue<SelectOptionType>) => {
                                setTyyppiFiltteri(values);
                                let tyyppi: string | undefined = undefined;
                                if (values?.length > 0) {
                                    tyyppi = values.map((val) => val?.label).join(', ');
                                }
                                setFilter('Tyyppi', tyyppi || undefined);
                            }}
                            isMulti
                            value={tyyppiFiltteri}
                            options={ryhmatyypitOptions}
                            styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
                        />
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_RYHMAN_KAYTTOTARKOITUS')}</label>
                        <Select
                            id={'RYHMAN_KAYTTOTARKOITUS_SELECT'}
                            onChange={(values: MultiValue<SelectOptionType>) => {
                                setKayttoRyhmatFiltteri(values);
                                let kayttoryhmat: string | undefined = undefined;
                                if (values?.length > 0) {
                                    kayttoryhmat = values.map((val) => val?.label).join(', ');
                                }
                                setFilter('Kayttotarkoitus', kayttoryhmat || undefined);
                            }}
                            isMulti
                            value={kayttoRyhmatFiltteri}
                            options={kayttoRyhmatOptions}
                            styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
                        />
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_RYHMAT_TILA')}</label>
                        <Select
                            id={'RYHMAN_TILA_SELECT'}
                            onChange={(values: MultiValue<SelectOptionType>) => {
                                setTilaFiltteri(values);
                                setFilter('status', (values && values.map((val) => val?.label)) || undefined);
                            }}
                            isMulti
                            value={tilaFiltteri}
                            options={ryhmanTilaOptions}
                            styles={{ menuPortal: (base) => ({ ...base, zIndex: 9999 }) }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

const NormaaliTaulukko = ({ ryhmatData = [], ryhmatColumns = [], useHakuFiltteri = false }: NormaaliTaulukkoProps) => {
    const [i18n] = useAtom(languageAtom);
    const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
    const [globalFilter, setGlobalFilter] = useState<string | undefined>(undefined);
    const [sorting, setSorting] = useState<SortingState>([
        {
            id: 'Nimi',
            desc: false,
        },
    ]);
    const [pagination, setPagination] = useState<PaginationState>({
        pageIndex: 0,
        pageSize: 10,
    });

    const { data, columns } = chooseTaulukkoData(ryhmatData, ryhmatColumns);
    const table = useReactTable({
        columns,
        data,
        state: {
            columnFilters,
            globalFilter,
            pagination,
            sorting,
        },
        onColumnFiltersChange: setColumnFilters,
        onGlobalFilterChange: setGlobalFilter,
        onPaginationChange: setPagination,
        onSortingChange: setSorting,
        defaultColumn: {
            cell: ({ getValue }) => getValue() as React.ReactNode,
        },
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        globalFilterFn: 'includesString',
    });
    const pageIndex = pagination.pageIndex;
    const pageSize = pagination.pageSize;
    const pageOptions = Array.from({ length: table.getPageCount() }, (_, index) => index);
    const setFilter = React.useCallback(
        (columnId: string, updater: string | undefined | (string | null | undefined)[]) => {
            table.getColumn(columnId)?.setFilterValue(updater);
        },
        [table]
    );
    return (
        <div>
            {useHakuFiltteri && (
                <div>
                    <Hakufiltterit
                        globalFilter={globalFilter}
                        setGlobalFilter={setGlobalFilter}
                        setFilter={setFilter}
                    />
                </div>
            )}
            <table style={{ width: '100%', borderSpacing: 0 }}>
                <thead>
                    {table.getHeaderGroups().map((headerGroup) => (
                        <tr key={headerGroup.id}>
                            {headerGroup.headers.map((header) => (
                                <th
                                    className={getColumnClassName(header.column.columnDef)}
                                    key={header.id}
                                    style={{ textAlign: 'left', borderBottom: '1px solid rgba(151,151,151,0.5)' }}
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
                    {table.getRowModel().rows.map((row, index) => {
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
                    })}
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
                                <Button key={option} onClick={() => table.setPageIndex(option)}>
                                    {option + 1}
                                </Button>
                            );
                        return (
                            <Button
                                key={option}
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
                        {[10, 20, 30, 40, 50].map((pageSize) => (
                            <option key={pageSize} value={pageSize}>
                                {pageSize}
                            </option>
                        ))}
                    </select>
                </div>
            </div>
        </div>
    );
};

export default NormaaliTaulukko;
