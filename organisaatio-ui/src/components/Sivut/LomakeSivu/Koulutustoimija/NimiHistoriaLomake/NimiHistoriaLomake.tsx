import * as React from 'react';
import { useContext } from 'react';
import styles from './NimiHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import { HistoriaTaulukkoData, Nimi, OrganisaationNimetNimi } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';
import { Column } from 'react-table';

type nimiHistoriaProps = {
    nimet: OrganisaationNimetNimi[];
};

export default function NimiHistoriaLomake(props: nimiHistoriaProps) {
    const { i18n } = useContext(LanguageContext);
    const { nimet } = props;

    const columns = [
        {
            Header: i18n.translate('NIMIHISTORIA_NIMEN_VOIMASSAOLO'),
            accessor: 'alkuPvm',
        },
        {
            Header: i18n.translate('NIMIHISTORIA_NIMI'),
            Cell: ({ row }: { row: { original: { nimi: Nimi } } }) =>
                Object.keys(row.original.nimi).map((k, i) => (
                    <span key={`nimihistoria_${k}`}>{`${row.original.nimi[k]} [${k}]${
                        Object.keys(row.original.nimi).length - 1 > i ? ', ' : ''
                    }`}</span>
                )),
        },
    ] as Column<OrganisaationNimetNimi | HistoriaTaulukkoData>[];
    return (
        <div className={styles.UloinKehys}>
            <YksinkertainenTaulukko data={nimet} tableColumns={columns} />
        </div>
    );
}
