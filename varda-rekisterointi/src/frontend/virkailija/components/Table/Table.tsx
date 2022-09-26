import * as React from 'react';
import styles from './Table.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
import {
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    useReactTable,
} from '@tanstack/react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { useContext } from 'react';
import { LanguageContext } from '../../../contexts';

type TableProps = {
    columns: any[];
    data: any[];
    tila: string[];
    handleTilaChange: (tila: string) => void;
};
enum tilaTypes {
    KASITTELYSSA = 'kÄSITTELYSSÄ',
    HYVAKSYTTY = 'HYVÄKSYTTY',
    HYLATTY = 'HYLÄTTY',
}
// TODO tyypit
export const Table = ({ columns, data, handleTilaChange, tila }: TableProps) => {
    const [rowSelection, setRowSelection] = React.useState({});
    const [globalFilter, setGlobalFilter] = React.useState('');
    const { i18n } = useContext(LanguageContext);

    const table = useReactTable({
        data,
        columns,
        state: {
            rowSelection,
            globalFilter,
            columnVisibility: { tila: false },
        },
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
    });

    return (
        <div className={styles.tableContainer}>
            <div className={styles.headerRow}>
                <input
                    value={globalFilter ?? ''}
                    onChange={(e) => setGlobalFilter(e.target.value)}
                    className={styles.filterInput}
                    placeholder="TODO"
                />
                <div>
                    {Object.keys(tilaTypes).map((key) => (
                        <Button
                            variant={tila.includes(key) ? 'contained' : 'outlined'}
                            onClick={() => {
                                handleTilaChange(key);
                            }}
                        >
                            {i18n.translate(`TAULUKKO_TILA_${key}`)}
                        </Button>
                    ))}
                </div>
            </div>
            <table className={styles.tableElement}>
                <thead className={styles.tHead}>
                    {table.getHeaderGroups().map((headerGroup: any) => (
                        <tr key={headerGroup.id}>
                            {headerGroup.headers.map((header: any) => (
                                <th key={header.id} colSpan={header.colSpan}>
                                    {header.isPlaceholder
                                        ? null
                                        : flexRender(header.column.columnDef.header, header.getContext())}
                                </th>
                            ))}
                        </tr>
                    ))}
                </thead>
                <tbody>
                    {table.getRowModel().rows.map((row: any) => {
                        return (
                            <tr key={row.id}>
                                {row.getVisibleCells().map((cell: any) => {
                                    return (
                                        <td key={cell.id}>
                                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                        </td>
                                    );
                                })}
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
};
