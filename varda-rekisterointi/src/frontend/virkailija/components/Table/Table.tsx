import React from 'react';
import SearchIcon from '@material-ui/icons/Search';
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
import { ButtonGroup } from '../../ButtonGroup';

type TableProps = {
    columns: any[];
    data: any[];
};
enum tilaTypes {
    KASITTELYSSA = 'KÄSITTELYSSÄ',
    HYVAKSYTTY = 'HYVÄKSYTTY',
    HYLATTY = 'HYLÄTTY',
}
// TODO tyypit
export const Table = ({ columns, data }: TableProps) => {
    const [rowSelection, setRowSelection] = React.useState({});
    const [globalFilter, setGlobalFilter] = React.useState('');

    const { i18n } = useContext(LanguageContext);
    const [tilaFilter, setTilaFilter] = React.useState<string>('');

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
                    {Object.keys(tilaTypes).map((key) => (
                        <Button
                            variant={tilaFilter === key ? 'contained' : 'outlined'}
                            onClick={() => {
                                tilaFilter === key ? setTilaFilter('') : setTilaFilter(key);
                            }}
                        >
                            {i18n.translate(`TAULUKKO_TILA_${key}`)}
                        </Button>
                    ))}
                </ButtonGroup>
            </div>
            <table className={styles.tableElement}>
                <thead className={styles.tHead}>
                    {table.getHeaderGroups().map((headerGroup: any) => (
                        <tr key={headerGroup.id}>
                            {headerGroup.headers.map((header: any) => (
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
                    {table.getRowModel().rows.map((row: any) => {
                        return (
                            <tr key={row.id}>
                                {row.getVisibleCells().map((cell: any) => {
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
        </div>
    );
};
