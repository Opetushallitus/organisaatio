import * as React from 'react';
import styles from './NormaaliTaulukko.module.css';
import {
    useGlobalFilter,
    useExpanded,
    usePagination,
    useTable,
    useSortBy,
    useFilters,
    Column,
    TableInstance,
    FilterValue,
} from 'react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import searchIcon from '@iconify/icons-fa-solid/search';
import Select from '@opetushallitus/virkailija-ui-components/Select';
import { Icon } from '@iconify/react';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { useContext } from 'react';
import { KoodistoContext, LanguageContext } from '../../contexts/contexts';
import { useState } from 'react';
import { Ryhma, SelectOptionType, YhteystietoTyyppi } from '../../types/types';
import { ValueType } from 'react-select';

// TODO yhtenäistä!
const mapPaginationSelectors = (index) => {
    if (index < 3) return [0, 5];
    return [index - 2, index + 3];
};

export type NormaaliTaulukkoProps = {
    ryhmatData?: Ryhma[];
    ryhmatColumns?: Column<Ryhma>[];
    yhteystietoTyypitData?: YhteystietoTyyppi[];
    yhteystietotyypitColumns?: Column<YhteystietoTyyppi>[];
    useHakuFiltteri?: boolean;
};

export type FiltteritProps = {
    setFilter: (columnId: string, updater: string | undefined | (string | null | undefined)[]) => void;
    setGlobalFilter: (filterValue: FilterValue) => void;
    globalFilter: string;
};

export const Hakufiltterit = ({ setFilter, globalFilter, setGlobalFilter }: FiltteritProps) => {
    const { i18n } = useContext(LanguageContext);
    const { ryhmaTyypitKoodisto, kayttoRyhmatKoodisto, ryhmanTilaKoodisto } = useContext(KoodistoContext);
    const [tyyppiFiltteri, setTyyppiFiltteri] = useState<
        ValueType<SelectOptionType>[] | ValueType<SelectOptionType> | undefined
    >();
    const [kayttoRyhmatFiltteri, setKayttoRyhmatFiltteri] = useState<ValueType<SelectOptionType>[] | undefined>();
    const [tilaFiltteri, setTilaFiltteri] = useState<ValueType<SelectOptionType>[] | undefined>();

    const ryhmatyypitOptions = ryhmaTyypitKoodisto.koodit().map((k) => {
        return { value: k.arvo, label: k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'] || '' };
    });
    const kayttoRyhmatOptions = kayttoRyhmatKoodisto.koodit().map((k) => {
        return { value: k.arvo, label: k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'] || '' };
    });
    const ryhmanTilaOptions = ryhmanTilaKoodisto.koodit().map((k) => {
        return { value: k.arvo, label: k.nimi['fi'] || k.nimi['sv'] || k.nimi['en'] || '' };
    });

    return (
        <div>
            <div className={styles.FiltteriContainer}>
                <div className={styles.FiltteriRivi}>
                    <div className={styles.FiltteriInputOsa}>
                        <Input
                            placeholder={i18n.translate('RYHMAT_HAKU_PLACEHOLDER')}
                            onChange={(e) => {
                                setGlobalFilter(e.target.value || undefined); // Set undefined to remove the filter entirely
                            }}
                            value={globalFilter}
                            suffix={<Icon color="#999999" icon={searchIcon} />}
                        />
                    </div>
                </div>
                <div className={styles.FiltteriRivi}>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_RYHMAN_TYYPPI')}</label>
                        <Select
                            onChange={(
                                values: ValueType<SelectOptionType>[] | ValueType<SelectOptionType> | undefined
                            ) => {
                                setTyyppiFiltteri(values as ValueType<SelectOptionType>[] | undefined);
                                let tyyppi: string | undefined = undefined;
                                if (values && (values as ValueType<SelectOptionType>[]).length > 0) {
                                    tyyppi = (values as ValueType<SelectOptionType>[])
                                        .map((val) => val && (val as SelectOptionType).label)
                                        .join(', ');
                                }
                                setFilter('Tyyppi', tyyppi || undefined);
                            }}
                            isMulti
                            value={tyyppiFiltteri as ValueType<SelectOptionType>}
                            options={ryhmatyypitOptions}
                        />
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_RYHMAN_KAYTTOTARKOITUS')}</label>
                        <Select
                            onChange={(
                                values: ValueType<SelectOptionType>[] | ValueType<SelectOptionType> | undefined
                            ) => {
                                setKayttoRyhmatFiltteri(values as ValueType<SelectOptionType>[] | undefined);
                                let kayttoryhmat: string | undefined = undefined;
                                if (values && (values as ValueType<SelectOptionType>[]).length > 0) {
                                    kayttoryhmat = (values as ValueType<SelectOptionType>[])
                                        .map((val) => val && (val as SelectOptionType).label)
                                        .join(', ');
                                }
                                setFilter('Kayttotarkoitus', kayttoryhmat || undefined);
                            }}
                            isMulti
                            value={kayttoRyhmatFiltteri as ValueType<SelectOptionType>}
                            options={kayttoRyhmatOptions}
                        />
                    </div>
                    <div className={styles.Kentta}>
                        <label>{i18n.translate('RYHMAT_RYHMAT_TILA')}</label>
                        <Select
                            onChange={(
                                values: ValueType<SelectOptionType>[] | ValueType<SelectOptionType> | undefined
                            ) => {
                                setTilaFiltteri(values as ValueType<SelectOptionType>[] | undefined);
                                setFilter(
                                    'status',
                                    (values &&
                                        (values as ValueType<SelectOptionType>[]).map(
                                            (val) => val && (val as SelectOptionType).label
                                        )) ||
                                        undefined
                                );
                            }}
                            isMulti
                            value={tilaFiltteri as ValueType<SelectOptionType>}
                            options={ryhmanTilaOptions}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

const NormaaliTaulukko = ({
    ryhmatData = [],
    yhteystietoTyypitData = [],
    ryhmatColumns = [],
    yhteystietotyypitColumns = [],
    useHakuFiltteri = false,
}: NormaaliTaulukkoProps) => {
    const { i18n } = useContext(LanguageContext);

    let data = [] as any; // TODO check type casting bc i could not get | to work.
    let columns = [] as any;
    if (ryhmatData && ryhmatData.length > 0) {
        data = ryhmatData;
        columns = ryhmatColumns;
    } else if (yhteystietoTyypitData && yhteystietoTyypitData.length > 0) {
        data = yhteystietoTyypitData;
        columns = yhteystietotyypitColumns;
    }

    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        prepareRow,
        page, // Instead of using 'rows', we'll use page,
        // which has only the rows for the active page

        globalFilter,
        setGlobalFilter,
        setFilter,
        canPreviousPage,
        canNextPage,
        pageOptions,
        gotoPage,
        nextPage,
        previousPage,
        setPageSize,
        state: { pageIndex, pageSize },
    } = useTable(
        {
            columns,
            data,
            initialState: {
                pageIndex: 0,
                sortBy: [
                    {
                        id: 'Nimi',
                        desc: false,
                    },
                ],
            },
        },
        useFilters,
        useGlobalFilter,
        useSortBy,
        useExpanded,
        usePagination
    ) as TableInstance<YhteystietoTyyppi> | TableInstance<Ryhma>;
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
            <table {...getTableProps()} style={{ width: '100%', borderSpacing: 0 }}>
                <thead>
                    {headerGroups.map((headerGroup) => (
                        <tr {...headerGroup.getHeaderGroupProps()}>
                            {headerGroup.headers.map((column) => (
                                <th
                                    {...column.getHeaderProps({
                                        className: column.collapse ? styles.collapse : '',
                                    })}
                                    style={{ textAlign: 'left', borderBottom: '1px solid rgba(151,151,151,0.5)' }}
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
                                {row.cells.map((cell) => {
                                    return (
                                        <td
                                            {...cell.getCellProps({
                                                className: cell.column.collapse ? styles.collapse : '',
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
                    <Button variant="text" color="secondary" onClick={() => previousPage()} disabled={!canPreviousPage}>
                        <Icon icon={chevronLeft} />
                    </Button>
                    {pageOptions.slice(...mapPaginationSelectors(pageIndex)).map((option) => {
                        if (option === pageIndex)
                            return (
                                <Button key={pageIndex} onClick={() => gotoPage(option)}>
                                    {option + 1}
                                </Button>
                            );
                        return (
                            <Button key={pageIndex} variant="text" color="secondary" onClick={() => gotoPage(option)}>
                                {option + 1}
                            </Button>
                        );
                    })}
                    <Button variant="text" color="secondary" onClick={() => nextPage()} disabled={!canNextPage}>
                        <Icon icon={chevronRight} />
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