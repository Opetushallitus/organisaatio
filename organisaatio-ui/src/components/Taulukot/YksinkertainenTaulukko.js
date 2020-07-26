import * as React from 'react';
import {
    useTable,
} from 'react-table';

export default function YksinkertainenTaulukko({ data: inputData = [], tableColumns = [] }) {
    const columns = React.useMemo(
        () => tableColumns,
        [tableColumns]
    );
  const data = React.useMemo(
    () => inputData,
    [inputData]
  );
    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        prepareRow,
        rows,
    } = useTable({ columns, data });
    return(
          <table {...getTableProps()} style={{ borderSpacing: 0 }}>
            <thead>
            {headerGroups.map(headerGroup => (
              <tr {...headerGroup.getHeaderGroupProps()}>
                {headerGroup.headers.map(column => (
                  <th {...column.getHeaderProps()}
                      style={{ textAlign: 'left', borderBottom: '1px solid rgba(151,151,151,0.5)'}}
                  >
                    {column.render('Header')}
                  </th>
                ))}
              </tr>
            ))}
            </thead>
              <tbody {...getTableBodyProps()}>
              {rows.map((row, index) => {
                prepareRow(row);
                return (
                  <tr {...row.getRowProps()} style={{ height: '3.125rem' }}>
                    {row.cells.map(cell => {
                      return <td {...cell.getCellProps()}
                                 style={{
                                   background: index % 2 === 0 ? '#F5F5F5': '#FFFFFF',
                                 }}
                      >{cell.render('Cell')}</td>
                    })}
                  </tr>
                )
              })}
              </tbody>
          </table>
    );
}