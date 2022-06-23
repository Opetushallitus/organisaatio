import * as React from 'react';
import { useState } from 'react';
import styles from './NimiHistoriaLomake.module.css';
import YksinkertainenTaulukko from '../../../../Taulukot/YksinkertainenTaulukko';
import { HistoriaTaulukkoData, UiOrganisaationNimetNimi } from '../../../../../types/types';
import { Column } from 'react-table';
import NimiHistoriaNimi from './NimiHistoriaNimi';
import { deleteOrganisaatioNimi } from '../../../../../api/organisaatio';
import Loading from '../../../../Loading/Loading';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../../api/lokalisaatio';
import moment from 'moment';

type nimiHistoriaProps = {
    nimet: UiOrganisaationNimetNimi[];
    handleNimiMuutos: () => void;
    oid: string;
};

export default function NimiHistoriaLomake(props: nimiHistoriaProps) {
    const [i18n] = useAtom(languageAtom);
    const { nimet, handleNimiMuutos, oid } = props;
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const sortedNimet = [...nimet].sort((a, b) =>
        moment(a.alkuPvm, 'D.M.YYYY').isBefore(moment(b.alkuPvm, 'D.M.YYYY')) ? 1 : -1
    );
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
            <YksinkertainenTaulukko data={sortedNimet} tableColumns={columns} />
        </div>
    );
}
