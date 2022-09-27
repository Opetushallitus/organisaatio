import * as React from 'react';
import { useContext } from 'react';
import styles from './Table.module.css';
import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    Row,
    useReactTable,
} from '@tanstack/react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { LanguageContext } from '../../../contexts';
import PaatosKontrollit from '../../PaatosKontrollit';
import { Rekisterointihakemus, Tila } from '../../rekisterointihakemus';

type TableProps = {
    columns: ColumnDef<Rekisterointihakemus>[];
    data: Rekisterointihakemus[];
};

const filterOnlyKasittelyssa = (rows: Row<Rekisterointihakemus>[]) => {
    return rows
        .filter((rh: Row<Rekisterointihakemus>) => rh.original.tila === Tila.KASITTELYSSA)
        .map((r) => r.original);
};
// TODO tyypit
export const Table = ({ columns, data }: TableProps) => {
    const [rowSelection, setRowSelection] = React.useState({});
    const [globalFilter, setGlobalFilter] = React.useState('');

    const { i18n } = useContext(LanguageContext);
    const [tilaFilter, setTilaFilter] = React.useState<string>(Tila.KASITTELYSSA);

    const table = useReactTable({
        data,
        columns,
        state: {
            rowSelection,
            globalFilter,
            columnFilters: [{ id: 'tila', value: tilaFilter }],
            columnVisibility: { tila: false },
        },
        onRowSelectionChange: setRowSelection,
        getCoreRowModel: getCoreRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
    });

    const selectedRows = filterOnlyKasittelyssa(table.getSelectedRowModel().rows);

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
                    {Object.keys(Tila).map((key) => (
                        <Button
                            variant={tilaFilter === key ? 'contained' : 'outlined'}
                            onClick={() => {
                                tilaFilter === key ? setTilaFilter('') : setTilaFilter(key);
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
            {(tilaFilter === Tila.KASITTELYSSA || selectedRows.length > 0) && (
                <PaatosKontrollit valitut={selectedRows} valitutKasiteltyCallback={() => {}} />
            )}
        </div>
    );
};
