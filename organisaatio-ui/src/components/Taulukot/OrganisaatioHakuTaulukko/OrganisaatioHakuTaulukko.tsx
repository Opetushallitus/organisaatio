import * as React from 'react';
import { useContext } from 'react';
import styles from './OrganisaatioHakuTaulukko.module.css';
import { Column, useExpanded, useGlobalFilter, usePagination, useSortBy, useTable } from 'react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { Icon } from '@iconify/react';
import chevronLeft from '@iconify/icons-fa-solid/chevron-left';
import chevronRight from '@iconify/icons-fa-solid/chevron-right';
import { LanguageContext } from '../../../contexts/contexts';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import searchIcon from '@iconify/icons-fa-solid/search';
import Checkbox from '@opetushallitus/virkailija-ui-components/Checkbox';
import { Organisaatio } from '../../../types/types';

const mapPaginationSelectors = (index) => {
    if (index < 3) return [0, 5];
    return [index - 2, index + 3];
};

type OrganisaatioHakuTaulukkoProps = {
    isOPHVirkailija: boolean;
    data: Organisaatio[];
    tableColumns: Column<Organisaatio>[];
    naytaPassivoidut: boolean;
    setNaytaPassivoidut: (value: boolean) => void;
    omatOrganisaatiotSelected: boolean;
    setOmatOrganisaatiotSelected: (value: boolean) => void;
};

function Hakufiltterit({
    globalFilter,
    setGlobalFilter,
    naytaPassivoidut,
    setNaytaPassivoidut,
    i18n,
    isOPHVirkailija,
    omatOrganisaatiotSelected,
    setOmatOrganisaatiotSelected,
}) {
    return (
        <div>
            {!isOPHVirkailija && (
                <div>
                    <Button
                        className={styles.KoulutustoimijaNappi}
                        color={'primary'}
                        variant={!omatOrganisaatiotSelected ? 'outlined' : 'contained'}
                        onClick={() => setOmatOrganisaatiotSelected(true)}
                    >
                        {i18n.translate('TAULUKKO_OMAT_ORGANISAATIOT')}
                    </Button>
                    <Button
                        onClick={() => setOmatOrganisaatiotSelected(false)}
                        className={styles.KoulutustoimijaNappi}
                        color={'primary'}
                        variant={omatOrganisaatiotSelected ? 'outlined' : 'contained'}
                    >
                        {i18n.translate('TAULUKKO_KAIKKI_ORGANISAATIOT')}
                    </Button>
                </div>
            )}
            <div className={styles.FiltteriContainer}>
                <div className={styles.FiltteriInputOsa}>
                    <Input
                        placeholder={i18n.translate('TAULUKKO_TOIMIJA_HAKU_PLACEHOLDER')}
                        onChange={(e) => {
                            setGlobalFilter(e.target.value || undefined); // Set undefined to remove the filter entirely
                        }}
                        value={globalFilter}
                        suffix={<Icon color="#999999" icon={searchIcon} />}
                    />
                    <Checkbox
                        type="checkbox"
                        checked={naytaPassivoidut}
                        onChange={(e) => {
                            setNaytaPassivoidut(e.target.checked);
                        }}
                    >
                        {i18n.translate('TAULUKKO_CHECKBOX_NAYTA_PASSIVOIDUT')}
                    </Checkbox>
                </div>
                <Button variant="outlined" className={styles.LisatiedotNappi}>
                    ?
                </Button>
            </div>
        </div>
    );
}

export default function OrganisaatioHakuTaulukko({
    isOPHVirkailija,
    data: inputData = [],
    tableColumns = [],
    naytaPassivoidut = false,
    setNaytaPassivoidut,
    omatOrganisaatiotSelected,
    setOmatOrganisaatiotSelected,
}: OrganisaatioHakuTaulukkoProps) {
    const { i18n } = useContext(LanguageContext);
    const columns = React.useMemo(() => tableColumns, [tableColumns]);
    const data = React.useMemo(() => inputData, [inputData]);

    /*const filterTypes = React.useMemo(
    () => ({
      text: (rows, id, filterValue) => {
        return rows.filter(row => {
          const rowValue = row.values[id];
          return rowValue !== undefined
            ? String(rowValue)
              .toLowerCase()
              .startsWith(String(filterValue).toLowerCase())
            : true;
        });
      }
    }),
    []
  );

   */
    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        prepareRow,
        page, // Instead of using 'rows', we'll use page,
        // which has only the rows for the active page
        globalFilter,
        setGlobalFilter,

        // The rest of these things are super handy, too ;)
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
        useGlobalFilter,
        useSortBy,
        useExpanded,
        usePagination
    );
    return (
        <div>
            <Hakufiltterit
                omatOrganisaatiotSelected={omatOrganisaatiotSelected}
                setOmatOrganisaatiotSelected={setOmatOrganisaatiotSelected}
                isOPHVirkailija={isOPHVirkailija}
                i18n={i18n}
                globalFilter={globalFilter}
                setGlobalFilter={setGlobalFilter}
                naytaPassivoidut={naytaPassivoidut}
                setNaytaPassivoidut={setNaytaPassivoidut}
            />
            <table {...getTableProps()} style={{ width: '100%', borderSpacing: 0 }}>
                <thead>
                    {headerGroups.map((
                        headerGroup: any //TODO remove any and take care of collapse
                    ) => (
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
                                {row.cells.map((cell: any) => {
                                    //TODO remove any and take care of collapse
                                    return (
                                        <td
                                            {...cell.getCellProps({
                                                className: cell.row.collapse ? styles.collapse : '',
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
                                <Button key={option + 1} onClick={() => gotoPage(option)}>
                                    {option + 1}
                                </Button>
                            );
                        return (
                            <Button key={option + 1} variant="text" color="secondary" onClick={() => gotoPage(option)}>
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
}
