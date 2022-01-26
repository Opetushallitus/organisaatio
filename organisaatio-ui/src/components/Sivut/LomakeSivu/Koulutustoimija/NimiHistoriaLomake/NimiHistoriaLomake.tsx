import * as React from 'react';
import { useContext, useState } from 'react';
import styles from './NimiHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import { HistoriaTaulukkoData, UiOrganisaationNimetNimi } from '../../../../../types/types';
import { Column } from 'react-table';
import NimiHistoriaNimi from './NimiHistoriaNimi';
import { deleteOrganisaatioNimi } from '../../../../../api/organisaatio';
import { LanguageContext } from '../../../../../contexts/LanguageContext';
import Loading from '../../../../Loading/Loading';

type nimiHistoriaProps = {
    nimet: UiOrganisaationNimetNimi[];
    handleNimiMuutos: () => void;
    oid: string;
};

export default function NimiHistoriaLomake(props: nimiHistoriaProps) {
    const { i18n } = useContext(LanguageContext);
    const { nimet, handleNimiMuutos, oid } = props;
    const [isLoading, setIsLoading] = useState<boolean>(false);

    async function handleDeleteNimi(nimi: UiOrganisaationNimetNimi) {
        setIsLoading(true);
        try {
            if (window.confirm(i18n.translate('NIMIHISTORIA_CONFIRM_DELETE_SCHEDULED_NIMI'))) {
                await deleteOrganisaatioNimi(oid, nimi);
                return handleNimiMuutos();
            }
        } finally {
            setIsLoading(false);
        }
    }

    const columns = [
        {
            Header: i18n.translate('NIMIHISTORIA_NIMEN_VOIMASSAOLO'),
            accessor: 'alkuPvm',
        },
        {
            Header: i18n.translate('NIMIHISTORIA_NIMI'),
            Cell: ({
                row: {
                    original: { nimi, alkuPvm, version },
                },
            }: {
                row: { original: UiOrganisaationNimetNimi };
            }) => (
                <NimiHistoriaNimi handleDeleteNimi={handleDeleteNimi} nimi={nimi} alkuPvm={alkuPvm} version={version} />
            ),
        },
    ] as Column<UiOrganisaationNimetNimi | HistoriaTaulukkoData>[];
    if (isLoading) {
        return <Loading />;
    }
    return (
        <div className={styles.UloinKehys}>
            <YksinkertainenTaulukko data={nimet} tableColumns={columns} />
        </div>
    );
}
