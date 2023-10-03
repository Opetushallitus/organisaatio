import * as React from 'react';
import { Column, useTable } from 'react-table';
import { HistoriaTaulukkoData, UiOrganisaationNimetNimi } from '../../types/types';

export default function YksinkertainenTaulukko({
    data: inputData = [],
    tableColumns = [],
}: {
    data: (HistoriaTaulukkoData | UiOrganisaationNimetNimi)[];
    tableColumns: Column<HistoriaTaulukkoData | UiOrganisaationNimetNimi>[];
}) {
    const columns = React.useMemo(() => tableColumns, [tableColumns]);
    const data = React.useMemo(() => inputData, [inputData]);
    const { getTableProps, getTableBodyProps, headerGroups, prepareRow, rows } = useTable<
        HistoriaTaulukkoData | UiOrganisaationNimetNimi
    >({ columns, data });
    return (
        <table {...getTableProps()} style={{ borderSpacing: 0, paddingTop: 20 }}>
            <thead>
                {headerGroups.map((headerGroup) => (
                    <tr {...headerGroup.getHeaderGroupProps()} key={headerGroup.getHeaderGroupProps().key}>
                        {headerGroup.headers.map((column) => (
                            <th
                                {...column.getHeaderProps()}
                                key={column.getHeaderProps().key}
                                style={{ textAlign: 'left', borderBottom: '1px solid rgba(151,151,151,0.5)' }}
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
                        <tr {...row.getRowProps()} key={row.getRowProps().key} style={{ height: '3.125rem' }}>
                            {row.cells.map((cell) => {
                                return (
                                    <td
                                        {...cell.getCellProps()}
                                        key={cell.getCellProps().key}
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
    );
}
