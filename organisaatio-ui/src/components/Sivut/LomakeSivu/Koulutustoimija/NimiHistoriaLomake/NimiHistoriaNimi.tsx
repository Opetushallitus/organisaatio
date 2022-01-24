import * as React from 'react';
import { LocalDate, Nimi, UiOrganisaationNimetNimi } from '../../../../../types/types';
import styles from './NimiHistoriaNimi.module.css';
import { ReadOnlyNimi } from '../../LomakeFields/LomakeFields';
import moment from 'moment';
import IconWrapper from '../../../../IconWapper/IconWrapper';
import { useAtom } from 'jotai';
import { languageAtom } from '../../../../../api/lokalisaatio';

type nimiHistoriaNimiProps = {
    nimi: Nimi;
    alkuPvm: LocalDate;
    handleDeleteNimi: (nimi: UiOrganisaationNimetNimi) => void;
};

export default function NimiHistoriaNimi(props: nimiHistoriaNimiProps) {
    const [i18n] = useAtom(languageAtom);
    const { nimi, alkuPvm, handleDeleteNimi } = props;
    function handleDeleteClick() {
        handleDeleteNimi({ nimi, alkuPvm });
    }
    const isAlkuPvmInFuture = moment(alkuPvm, 'D.M.YYYY') > moment();
    return (
        <div className={styles.NimiHistoriaCell}>
            <ReadOnlyNimi value={nimi} />
            {isAlkuPvmInFuture && (
                <div
                    className={styles.DeleteBtn}
                    onClick={handleDeleteClick}
                    title={i18n.translate('POISTA_AJASTETTU_NIMENMUUTOS')}
                >
                    <IconWrapper
                        icon="ic:round-delete-forever"
                        height={'1.5rem'}
                        className={styles.DeleteIcon}
                        name={'POISTA_AJASTETTU_NIMENMUUTOS'}
                    >
                        {i18n.translate('POISTA_AJASTETTU_NIMENMUUTOS')}
                    </IconWrapper>
                </div>
            )}
        </div>
    );
}
