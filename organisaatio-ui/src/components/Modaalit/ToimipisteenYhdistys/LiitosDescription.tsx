import React, { useEffect, useMemo, useState } from 'react';
import { ApiOrganisaatio } from '../../../types/apiTypes';
import { searchOrganisation } from '../../../api/organisaatio';
import Spin from '@opetushallitus/virkailija-ui-components/Spin';
import { useTable } from 'react-table';
import { languageAtom } from '../../../api/lokalisaatio';
import { useAtom } from 'jotai';
import styled from 'styled-components';
import { organisaatioTyypitKoodistoAtom } from '../../../api/koodisto';
import { BodyRivi } from '../ModalFields/ModalFields';

const Styles = styled.div`
    table {
        border-collapse: collapse;
        margin-bottom: 2rem;
        th {
            text-align: left;
            border-bottom: 1px solid rgba(151, 151, 151, 0.5);
            padding: 0.5rem;
            font-weight: bold;
        }
        td {
            padding: 0.5rem;
        }
        tbody {
            border: 1px solid rgba(151, 151, 151, 0.5);
            tr:nth-child(even) {
                background: #ffffff;
            }
            tr:nth-child(odd) {
                background: #f5f5f5;
            }
        }
    }
`;

const flattenHierarchy = (p: ApiOrganisaatio[], c: ApiOrganisaatio) => [
    ...p,
    { ...c, subRows: undefined },
    ...(c.subRows?.reduce(flattenHierarchy, []) || []),
];

const LiitosDescription: React.FC<{ sourceOid: string }> = ({ sourceOid }) => {
    const [i18n] = useAtom(languageAtom);
    const [organisaatioTyypit] = useAtom(organisaatioTyypitKoodistoAtom);
    const [self, setSelf] = useState<ApiOrganisaatio | undefined>(undefined);
    const data = useMemo(
        () =>
            self?.subRows
                ?.reduce(flattenHierarchy, [])
                .sort((a, b) => i18n.translateNimi(a.nimi).localeCompare(i18n.translateNimi(b.nimi))) || [],
        [i18n, self?.subRows]
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
    const findSelfMemo = useMemo<(a: ApiOrganisaatio[], oid: string) => ApiOrganisaatio | undefined>(() => {
        const findSelf = (a: ApiOrganisaatio[], oid: string): ApiOrganisaatio | undefined => {
            if (a.length === 0) return undefined;
            return a[0]?.oid === oid ? a[0] : findSelf(a[0].subRows || [], oid);
        };
        return findSelf;
    }, []);
    useEffect(() => {
        (async () => {
            const searchResult = await searchOrganisation({
                oid: sourceOid,
            });
            const mySelf = findSelfMemo(searchResult, sourceOid);
            setSelf(mySelf);
        })();
    }, [findSelfMemo, sourceOid]);
    const { getTableProps, getTableBodyProps, headerGroups, rows, prepareRow } = useTable({
        columns,
        data,
    });

    if (!self || !data) {
        return <Spin />;
    }
    return (
        <>
            <BodyRivi>
                <Styles>
                    <table {...getTableProps()}>
                        <thead>
                            {headerGroups.map((headerGroup) => (
                                <tr {...headerGroup.getHeaderGroupProps()}>
                                    {headerGroup.headers.map((column) => (
                                        <th {...column.getHeaderProps()}>{column.render('Header')}</th>
                                    ))}
                                </tr>
                            ))}
                        </thead>
                        <tbody {...getTableBodyProps()}>
                            {rows.map((row, i) => {
                                prepareRow(row);
                                return (
                                    <tr {...row.getRowProps()}>
                                        {row.cells.map((cell) => {
                                            return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>;
                                        })}
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </Styles>
            </BodyRivi>
        </>
    );
};
export default LiitosDescription;
