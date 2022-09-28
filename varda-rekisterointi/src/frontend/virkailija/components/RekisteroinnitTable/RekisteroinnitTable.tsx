import React, { useMemo, useContext, HTMLProps } from 'react';
import { format, parseISO } from 'date-fns';
import { ColumnDef, Row, Table as TableType } from '@tanstack/react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';

import { Table } from '../Table/Table';
import { LanguageContext } from '../../../contexts';
import { Rekisterointihakemus } from '../../rekisterointihakemus';

import styles from './RekisteroinnitTable.module.css';

type RekisteroinnitTableProps = {
    rekisteroinnit: Rekisterointihakemus[];
};

const saapumisAikaFormat = 'd.M.y HH:mm';

function IndeterminateCheckbox({
    indeterminate,
    className = '',
    ...rest
}: { indeterminate?: boolean } & HTMLProps<HTMLInputElement>) {
    const ref = React.useRef<HTMLInputElement>(null!);

    React.useEffect(() => {
        if (typeof indeterminate === 'boolean') {
            ref.current.indeterminate = !rest.checked && indeterminate;
        }
    }, [ref, indeterminate, rest.checked]);

    return <input type="checkbox" ref={ref} className={className + ' cursor-pointer'} {...rest} />;
}

//TODO tyypitystä
export default function RekisteroinnitTable({ rekisteroinnit }: RekisteroinnitTableProps) {
    const { i18n } = useContext(LanguageContext);
    const data = useMemo<Rekisterointihakemus[]>(() => {
        rekisteroinnit.sort((a, b) => a.organisaatio.ytjNimi.nimi.localeCompare(b.organisaatio.ytjNimi.nimi));
        return [...rekisteroinnit];
    }, [rekisteroinnit]);

    const columns = useMemo<ColumnDef<Rekisterointihakemus>[]>(
        () =>
            [
                {
                    id: 'select',
                    header: ({ table }: { table: TableType<Rekisterointihakemus> }) => (
                        <IndeterminateCheckbox
                            {...{
                                checked: table.getIsAllRowsSelected(),
                                indeterminate: table.getIsSomeRowsSelected(),
                                onChange: table.getToggleAllRowsSelectedHandler(),
                            }}
                        />
                    ),
                    cell: ({ row }: { row: Row<Rekisterointihakemus> }) => (
                        <IndeterminateCheckbox
                            {...{
                                disabled: row.original.tila !== 'KASITTELYSSA' && true,
                                checked: row.original.tila !== 'KASITTELYSSA' ? false : row.getIsSelected(),
                                indeterminate: row.getIsSomeSelected(),
                                onChange: row.getToggleSelectedHandler(),
                            }}
                        />
                    ),
                    size: 20,
                },
                {
                    header: i18n.translate('TAULUKKO_ORGANISAATIO_NIMI_OTSIKKO'),
                    id: 'organisaationimi',
                    accessorFn: (values: Rekisterointihakemus) =>
                        values.organisaatio?.ytjNimi?.nimi || i18n.translate('TAULUKKO_NIMI_PUUTTUU_ORGANISAATIOLTA'),
                    size: 250,
                },
                {
                    header: i18n.translate('TAULUKKO_ORGANISAATIO_PUHELINNUMERO_OTSIKKO'),
                    accessorFn: (values: Rekisterointihakemus) =>
                        values.organisaatio?.yhteystiedot?.puhelinnumero ||
                        i18n.translate('TAULUKKO_PUHELINNUMERO_PUUTTUU_ORGANISAATIOLTA'),
                },
                {
                    header: i18n.translate('TAULUKKO_ORGANISAATIO_YTUNNUS_OTSIKKO'),
                    id: 'ytunnus',
                    accessorFn: (values: Rekisterointihakemus) =>
                        values.organisaatio?.ytunnus || i18n.translate('TAULUKKO_YTUNNUS_PUUTTUU_ORGANISAATIOLTA'),
                },
                rekisteroinnit[0].tyyppi === 'varda'
                    ? {
                          header: i18n.translate('TAULUKKO_KUNNAT_OTSIKKO'),
                          id: 'kunnat',
                          accessorFn: (values: Rekisterointihakemus) => values.kunnat.join(', '),
                      }
                    : (undefined as unknown as ColumnDef<Rekisterointihakemus>),
                {
                    header: i18n.translate('TAULUKKO_VASTAANOTETTU_OTSIKKO'),
                    id: 'vastaanotettu',
                    accessorFn: (values: Rekisterointihakemus) =>
                        format(parseISO(values.vastaanotettu), saapumisAikaFormat),
                },
                {
                    id: 'hyvaksynta',
                    cell: () => (
                        <div className={styles.hyvaksyntaButtonsContainer}>
                            <Button variant={'outlined'} onClick={() => alert('klikkasit hyväksyntää')}>
                                {i18n.translate('TAULUKKO_HYLKAA_HAKEMUS')}
                            </Button>
                            <Button onClick={() => alert('klikkasit hyväksyntää')}>
                                {i18n.translate('TAULUKKO_HYVAKSY_HAKEMUS')}
                            </Button>
                        </div>
                    ),
                },
                {
                    header: i18n.translate('TAULUKKO_HYLATTY_OTSIKKO'),
                    id: 'hylatty',
                    accessorFn: (values: Rekisterointihakemus) =>
                        values.paatos?.paatetty && format(parseISO(values.paatos?.paatetty), saapumisAikaFormat),
                },
                {
                    header: i18n.translate('TAULUKKO_HYVAKSYTTY_OTSIKKO'),
                    id: 'hyvaksytty',
                    accessorFn: (values: Rekisterointihakemus) =>
                        values.paatos?.paatetty && format(parseISO(values.paatos?.paatetty), saapumisAikaFormat),
                },
                {
                    enableColumnFilter: true,
                    accessorKey: 'tila',
                },
            ].filter((c) => !!c),
        [i18n, rekisteroinnit]
    );

    return <Table columns={columns} data={data} />;
}
