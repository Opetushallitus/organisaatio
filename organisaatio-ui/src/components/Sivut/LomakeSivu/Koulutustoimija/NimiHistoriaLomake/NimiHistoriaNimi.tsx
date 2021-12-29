import * as React from 'react';
import { LocalDate, Nimi, UiOrganisaationNimetNimi } from '../../../../../types/types';
import styles from './NimiHistoriaNimi.module.css';
import { ReadOnlyNimi } from '../../LomakeFields/LomakeFields';
import { Icon } from '@iconify/react';
import moment from 'moment';

type nimiHistoriaNimiProps = {
    nimi: Nimi;
    alkuPvm: LocalDate;
    handleDeleteNimi: (nimi: UiOrganisaationNimetNimi) => void;
};

export default function NimiHistoriaNimi(props: nimiHistoriaNimiProps) {
    const { nimi, alkuPvm, handleDeleteNimi } = props;

    function handleDeleteClick() {
        handleDeleteNimi({ nimi, alkuPvm });
    }

    const isAlkuPvmInFuture = moment(alkuPvm, 'D.M.YYYY') > moment();
    return (
        <div className={styles.NimiHistoriaCell}>
            <ReadOnlyNimi value={nimi} />
            {isAlkuPvmInFuture && (
                <div className={styles.DeleteBtn} onClick={handleDeleteClick}>
                    <Icon
                        fr={undefined}
                        icon="ic:round-delete-forever"
                        height={'1.5rem'}
                        className={styles.DeleteIcon}
                    />
                </div>
            )}
        </div>
    );
}
