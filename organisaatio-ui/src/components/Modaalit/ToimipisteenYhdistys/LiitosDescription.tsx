import React, { useEffect, useMemo, useState } from 'react';
import { OrganisaatioHakuOrganisaatio } from '../../../types/apiTypes';
import { getJalkelaiset } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { useTable } from 'react-table';
import { languageAtom } from '../../../api/lokalisaatio';
import { useAtom } from 'jotai';
import styles from './LiitosDescription.module.css';
import { organisaatioTyypitKoodistoAtom } from '../../../api/koodisto';
import { BodyRivi } from '../ModalFields/ModalFields';

const flattenHierarchy = (p: OrganisaatioHakuOrganisaatio[], c: OrganisaatioHakuOrganisaatio) => [
    ...p,
    { ...c, subRows: [] },
    ...c.subRows.reduce(flattenHierarchy, []),
];

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
    const columns = useMemo(
        () => [
            {
                Header: i18n.translate('LIITOS_TAULUKKO_NIMI'),
                id: 'nimi',
                accessor: (row) => {
                    return i18n.translateNimi(row.nimi);
                },
            },
            {
                Header: i18n.translate('LIITOS_TAULUKKO_OID'),
                id: 'oid',
                accessor: (row) => {
                    return row.oid;
                },
            },
            {
                Header: i18n.translate('LIITOS_TAULUKKO_TYYPIT'),
                id: 'tyypit',
                accessor: (row) => {
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
    const { getTableProps, getTableBodyProps, headerGroups, rows, prepareRow } = useTable({
        columns,
        data,
    });

    if (!organisaatiot || !data) {
        return <Spin />;
    }
    return (
        <BodyRivi>
            <table {...getTableProps()} className={styles.LiitosDescriptionTable}>
                <thead>
                    {headerGroups.map((headerGroup) => (
                        <tr {...headerGroup.getHeaderGroupProps()} key={headerGroup.getHeaderGroupProps().key}>
                            {headerGroup.headers.map((column) => (
                                <th {...column.getHeaderProps()} key={column.getHeaderProps().key}>
                                    {column.render('Header')}
                                </th>
                            ))}
                        </tr>
                    ))}
                </thead>
                <tbody {...getTableBodyProps()}>
                    {rows.map((row) => {
                        prepareRow(row);
                        return (
                            <tr {...row.getRowProps()} key={row.getRowProps().key}>
                                {row.cells.map((cell) => {
                                    return (
                                        <td {...cell.getCellProps()} key={cell.getCellProps().key}>
                                            {cell.render('Cell')}
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
