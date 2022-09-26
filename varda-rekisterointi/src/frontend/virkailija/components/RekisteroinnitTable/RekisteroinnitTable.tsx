import React, { useMemo, useRef, useContext, HTMLProps } from 'react';
import styles from './RekisteroinnitTable.module.css';
import { Table } from '../Table/Table';
import { LanguageContext } from '../../../contexts';
import { Rekisterointihakemus } from '../../rekisterointihakemus';

import { ColumnDef } from '@tanstack/react-table';
import Button from '@opetushallitus/virkailija-ui-components/Button';

type RekisteroinnitTableProps = {
    rekisteroinnit: Rekisterointihakemus[];
};

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
    }, [ref, indeterminate]);

    return <input type="checkbox" ref={ref} className={className + ' cursor-pointer'} {...rest} />;
}

//TODO tyypitystä
export default function RekisteroinnitTable({ rekisteroinnit }: RekisteroinnitTableProps) {
    const { i18n } = useContext(LanguageContext);
    const data = useMemo<Rekisterointihakemus[]>(() => {
        rekisteroinnit.sort((a, b) => a.organisaatio.ytjNimi.nimi.localeCompare(b.organisaatio.ytjNimi.nimi));
        return [...rekisteroinnit];
    }, [rekisteroinnit]);

    const columns = React.useMemo<ColumnDef<Rekisterointihakemus>[]>(
        () => [
            {
                id: 'select',
                header: ({ table }: { table: any }) => (
                    <IndeterminateCheckbox
                        {...{
                            checked: table.getIsAllRowsSelected(),
                            indeterminate: table.getIsSomeRowsSelected(),
                            onChange: table.getToggleAllRowsSelectedHandler(),
                        }}
                    />
                ),
                cell: ({ row }: { row: any }) => (
                    <div className="px-1">
                        <IndeterminateCheckbox
                            {...{
                                checked: row.getIsSelected(),
                                indeterminate: row.getIsSomeSelected(),
                                onChange: row.getToggleSelectedHandler(),
                            }}
                        />
                    </div>
                ),
            },
            {
                header: i18n.translate('TAULUKKO_ORGANISAATIO_NIMI_OTSIKKO'),
                id: 'organisaationimi',
                accessorFn: (values: Rekisterointihakemus) =>
                    values.organisaatio?.ytjNimi?.nimi || i18n.translate('TAULUKKO_NIMI_PUUTTUU_ORGANISAATIOLTA'),
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
            {
                header: i18n.translate('TAULUKKO_VASTAANOTETTU_OTSIKKO'),
                id: 'vastaanotettu',
                accessorKey: 'vastaanotettu',
            },
            {
                id: 'hyvaksynta',
                cell: (info) => (
                    <div className={styles.HyvaksyntaButtonsContainer}>
                        <Button onClick={() => alert('klikkasit hyväksyntää')}>
                            {i18n.translate('TAULUKKO_HYVAKSY_HAKEMUS')}
                        </Button>
                        <Button variant={'outlined'} onClick={() => alert('klikkasit hyväksyntää')}>
                            {i18n.translate('TAULUKKO_HYLKAA_HAKEMUS')}
                        </Button>
                    </div>
                ),
            },
            {
                enableColumnFilter: true,
                accessorKey: 'tila',
            },
        ],
        [i18n]
    );

    return (
        <>
            <Table columns={columns} data={data} />
        </>
    );
}
