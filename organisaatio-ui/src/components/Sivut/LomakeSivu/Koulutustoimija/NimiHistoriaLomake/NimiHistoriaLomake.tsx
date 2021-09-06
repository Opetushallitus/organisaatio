import * as React from 'react';
import { useContext } from 'react';
import styles from './NimiHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import { Organisaatio } from '../../../../../types/types';
import { LanguageContext } from '../../../../../contexts/contexts';

type nimiHistoriaProps = {
    nimet: any;
    handleOnChange: ({ name, value }: { name: keyof Organisaatio; value: any }) => void;
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
            Cell: ({ row }: any) =>
                Object.keys(row.original.nimi).map((k, i) => (
                    <span>{`${row.original.nimi[k]} [${k}]${
                        Object.keys(row.original.nimi).length - 1 > i ? ', ' : ''
                    }`}</span>
                )),
        },
    ];
    return (
        <div className={styles.UloinKehys}>
            <YksinkertainenTaulukko data={nimet} tableColumns={columns} />
        </div>
    );
}
