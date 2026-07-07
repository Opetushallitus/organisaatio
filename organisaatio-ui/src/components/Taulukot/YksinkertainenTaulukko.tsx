import * as React from 'react';
import { ColumnDef, flexRender, getCoreRowModel, useReactTable } from '@tanstack/react-table';
import { HistoriaTaulukkoData, UiOrganisaationNimetNimi } from '../../types/types';

type YksinkertainenTaulukkoData = HistoriaTaulukkoData | UiOrganisaationNimetNimi;

export default function YksinkertainenTaulukko({
    data: inputData = [],
    tableColumns = [],
}: {
    data: YksinkertainenTaulukkoData[];
    tableColumns: ColumnDef<YksinkertainenTaulukkoData>[];
}) {
    const columns = React.useMemo(() => tableColumns, [tableColumns]);
    const data = React.useMemo(() => inputData, [inputData]);
    const table = useReactTable({
        columns,
        data,
        defaultColumn: {
            cell: ({ getValue }) => getValue() as React.ReactNode,
        },
        getCoreRowModel: getCoreRowModel(),
    });
    return (
        <table style={{ borderSpacing: 0, paddingTop: 20 }}>
            <thead>
                {table.getHeaderGroups().map((headerGroup) => (
                    <tr key={headerGroup.id}>
                        {headerGroup.headers.map((header) => (
                            <th
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
                        <tr key={row.id} style={{ height: '3.125rem' }}>
                            {row.getVisibleCells().map((cell) => {
                                return (
                                    <td
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
    );
}
