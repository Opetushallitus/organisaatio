import React, { useContext, useState } from 'react';
import SearchIcon from '@material-ui/icons/Search';
import styles from './Table.module.css';
import Input from '@opetushallitus/virkailija-ui-components/Input';
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
import ApprovalButtonsContainer from '../ApprovalButtonsContainer/ApprovalButtonsContainer';
import { Rekisterointihakemus, Tila, TILAT } from '../../rekisterointihakemus';
import { ButtonGroup } from '../../ButtonGroup';

type TableProps = {
    columns: ColumnDef<Rekisterointihakemus>[];
    data: Rekisterointihakemus[];
};

const filterOnlyKasittelyssa = (rows: Row<Rekisterointihakemus>[]) => {
    return rows.filter((rh: Row<Rekisterointihakemus>) => rh.original.tila === 'KASITTELYSSA').map((r) => r.original);
};

export const Table = ({ columns, data }: TableProps) => {
    const { i18n } = useContext(LanguageContext);
    const [rowSelection, setRowSelection] = useState({});
    const [globalFilter, setGlobalFilter] = useState('');
    const [tilaFilter, setTilaFilter] = useState<Tila>('KASITTELYSSA');
    const kasittelyssa = data.filter((r) => r.tila === 'KASITTELYSSA');

    const table = useReactTable({
        data,
        columns,
        state: {
            rowSelection,
            globalFilter,
            columnFilters: [{ id: 'tila', value: tilaFilter }],
            columnVisibility: {
                tila: false,
                select: tilaFilter === 'KASITTELYSSA',
                hyvaksynta: tilaFilter === 'KASITTELYSSA',
                hylatty: tilaFilter === 'HYLATTY',
                hyvaksytty: tilaFilter === 'HYVAKSYTTY',
            },
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
                <div className={styles.filterInputContainer}>
                    <Input
                        value={globalFilter}
                        placeholder={i18n.translate('HAKU_PLACEHOLDER')}
                        suffix={<SearchIcon color="disabled" />}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => setGlobalFilter(e.target.value)}
                        className={styles.filterInput}
                    />
                </div>
                <ButtonGroup>
                    {TILAT.map((key) => (
                        <Button
                            className={tilaFilter === key ? styles.selectedTila : ''}
                            key={key}
                            variant={tilaFilter === key ? 'contained' : 'outlined'}
                            onClick={() => {
                                tilaFilter !== key && setTilaFilter(key);
                            }}
                        >
                            {i18n.translate(`TAULUKKO_TILA_${key}`)}
                            {key === 'KASITTELYSSA' && kasittelyssa.length > 0 ? ` (${kasittelyssa.length})` : ''}
                        </Button>
                    ))}
                </ButtonGroup>
            </div>
            <table className={styles.tableElement}>
                <thead className={styles.tHead}>
                    {table.getHeaderGroups().map((headerGroup) => (
                        <tr key={headerGroup.id}>
                            {headerGroup.headers.map((header) => (
                                <th key={header.id} colSpan={header.colSpan} className={styles.tHeadCell}>
                                    {header.isPlaceholder
                                        ? null
                                        : flexRender(header.column.columnDef.header, header.getContext())}
                                </th>
                            ))}
                        </tr>
                    ))}
                </thead>
                <tbody>
                    {table.getRowModel().rows.map((row, idx: number) => {
                        return (
                            <tr key={row.id} className={idx % 2 === 1 ? styles.evenRow : ''}>
                                {row.getVisibleCells().map((cell) => {
                                    return (
                                        <td key={cell.id} className={styles.tBodyCell}>
                                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                        </td>
                                    );
                                })}
                            </tr>
                        );
                    })}
                </tbody>
            </table>
            {(tilaFilter === 'KASITTELYSSA' || selectedRows.length > 0) && (
                <ApprovalButtonsContainer chosenRekisteroinnit={selectedRows} valitutKasiteltyCallback={() => {}} />
            )}
        </div>
    );
};
