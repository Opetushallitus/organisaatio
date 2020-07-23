import * as React from 'react';
import styles from './NormaaliTaulukko.module.css';
import {
  useExpanded, usePagination,
  useTable,
} from 'react-table';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import {Icon} from "@iconify/react";
import chevronLeft from "@iconify/icons-fa-solid/chevron-left";
import chevronRight from "@iconify/icons-fa-solid/chevron-right";
import {useContext} from "react";
import {LanguageContext} from "../../contexts/contexts";



const mapPaginationSelectors = (index) => {
  if (index < 3) return [0, 5];
  return [index-2, index+3];
}

export default function NormaaliTaulukko({ data: inputData = [], tableColumns = [] }) {
    const { i18n, language } = useContext(LanguageContext);
    const columns = React.useMemo(
        () => tableColumns,
        []
    );
    const data = React.useMemo(
      () => inputData,
      []
    );
    console.log('pöö', inputData, tableColumns);
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
      pageCount,
      gotoPage,
      nextPage,
      previousPage,
      setPageSize,
      state: { pageIndex, pageSize },
    } = useTable({ columns, data, initialState: { pageIndex: 0 } },
      useExpanded, usePagination);
      return(
        <div>
          <table {...getTableProps()} style={{ width: '100%', borderSpacing: 0 }}>
            <thead>
            {headerGroups.map(headerGroup => (
              <tr {...headerGroup.getHeaderGroupProps()}>
                {headerGroup.headers.map(column => (
                  <th
                    {...column.getHeaderProps({
                      className: column.collapse ? styles.collapse : '',
                    })}
                    style={{ textAlign: 'left', borderBottom: '1px solid rgba(151,151,151,0.5)'}}
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
                  {row.cells.map(cell => {
                    return (
                      <td
                        {...cell.getCellProps({
                          className: cell.column.collapse ? styles.collapse : '',
                        })}
                        style={{
                          background: index % 2 === 0 ? '#F5F5F5': '#FFFFFF',
                        }}
                      >
                        {cell.render('Cell')}
                      </td>
                    )
                  })}
                </tr>
              )
            })}
            </tbody>
          </table>
          <div className={styles.PaginationContainer}>
            <div className={styles.PaginationSivunvaihto}>
              <Button variant='text' color="secondary" onClick={() => previousPage()} disabled={!canPreviousPage}>
                <Icon icon={chevronLeft} />
              </Button>
              {pageOptions.slice(...mapPaginationSelectors(pageIndex)).map(option => {
                if (option === pageIndex) return (<Button onClick={() => gotoPage(option)}>
                  {option+1}
                </Button>);
                return (<Button variant='text' color="secondary" onClick={() => gotoPage(option)}>
                  {option+1}
                </Button>);
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
                onChange={e => {
                  setPageSize(Number(e.target.value))
                }}
              >
                {[10, 20, 30, 40, 50].map(pageSize => (
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