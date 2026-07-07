import React, { useEffect, useMemo, useState } from 'react';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import { getJalkelaiset } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { ColumnDef, flexRender, getCoreRowModel, useReactTable } from '@tanstack/react-table';
import { languageAtom } from '../../../api/lokalisaatio';
import { useAtom } from 'jotai';
import styles from './LiitosDescription.module.css';
import { organisaatioTyypitKoodistoAtom } from '../../../api/koodisto';
import { BodyRivi } from '../ModalFields/ModalFields';

const flattenHierarchy = (
    p: OrganisaatioHakuOrganisaatio[],
    c: OrganisaatioHakuOrganisaatio
): OrganisaatioHakuOrganisaatio[] => [...p, { ...c, subRows: [] }, ...c.subRows.reduce(flattenHierarchy, [])];

const LiitosDescription: React.FC<{ sourceOid: string }> = ({ sourceOid }) => {
    const [i18n] = useAtom(languageAtom);
    const [organisaatioTyypit] = useAtom(organisaatioTyypitKoodistoAtom);
    const [organisaatiot, setOrganisaatiot] = useState<OrganisaatioHakuOrganisaatio[]>([]);
    const data = useMemo(
        () =>
            organisaatiot
                .reduce(flattenHierarchy, [])
                .filter((a) => a.status === 'AKTIIVINEN')
                .sort((a, b) => i18n.translateNimi(a.nimi).localeCompare(i18n.translateNimi(b.nimi))) || [],
        [i18n, organisaatiot]
    );
    const columns = useMemo<ColumnDef<OrganisaatioHakuOrganisaatio>[]>(
        () => [
            {
                header: i18n.translate('LIITOS_TAULUKKO_NIMI'),
                id: 'nimi',
                accessorFn: (row) => {
                    return i18n.translateNimi(row.nimi);
                },
            },
            {
                header: i18n.translate('LIITOS_TAULUKKO_OID'),
                id: 'oid',
                accessorFn: (row) => {
                    return row.oid;
                },
            },
            {
                header: i18n.translate('LIITOS_TAULUKKO_TYYPIT'),
                id: 'tyypit',
                accessorFn: (row) => {
                    return row.organisaatiotyypit.map((a) => organisaatioTyypit.uri2Nimi(a)).join(', ');
                },
            },
        ],
        [i18n, organisaatioTyypit]
    );

    useEffect(() => {
        (async () => {
            const jalkelaiset = await getJalkelaiset({
                oid: sourceOid,
            });
            setOrganisaatiot(jalkelaiset);
        })();
    }, [sourceOid]);

    const table = useReactTable({
        columns,
        data,
        defaultColumn: {
            cell: ({ getValue }) => getValue() as React.ReactNode,
        },
        getCoreRowModel: getCoreRowModel(),
    });

    if (!organisaatiot || !data) {
        return <Spin />;
    }
    return (
        <BodyRivi>
            <table className={styles.LiitosDescriptionTable}>
                <thead>
                    {table.getHeaderGroups().map((headerGroup) => (
                        <tr key={headerGroup.id}>
                            {headerGroup.headers.map((header) => (
                                <th key={header.id}>
                                    {header.isPlaceholder
                                        ? null
                                        : flexRender(header.column.columnDef.header, header.getContext())}
                                </th>
                            ))}
                        </tr>
                    ))}
                </thead>
                <tbody>
                    {table.getRowModel().rows.map((row) => {
                        return (
                            <tr key={row.id}>
                                {row.getVisibleCells().map((cell) => {
                                    return (
                                        <td key={cell.id}>
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
        </BodyRivi>
    );
};
export default LiitosDescription;
