import * as React from 'react';
import { useContext } from 'react';
import { LanguageContext } from '../../../../../contexts/contexts';
import { LocalDate, Nimi, OrganisaationNimetNimi } from '../../../../../types/types';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import styles from './NimiHistoriaNimi.module.css';

type nimiHistoriaNimiProps = {
    nimi: Nimi;
    alkuPvm: LocalDate;
    handleDeleteNimi: (nimi: OrganisaationNimetNimi) => void;
};

export default function NimiHistoriaNimi(props: nimiHistoriaNimiProps) {
    const { i18n } = useContext(LanguageContext);
    const { nimi, alkuPvm, handleDeleteNimi } = props;

    function handleDeleteClick() {
        handleDeleteNimi({ nimi, alkuPvm });
    }

    const nimiKeys = Object.keys(nimi);
    const isAlkuPvmInFuture = new Date(alkuPvm) > new Date();
    return (
        <div className={styles.NimiHistoriaCell}>
            {nimiKeys.map((nimiKey, i) => (
                <span key={`nimihistoria_${nimiKey}`}>{`${nimi[nimiKey]} [${nimiKey}]${
                    nimiKeys.length - 1 > i ? ', ' : ''
                }`}</span>
            ))}
            {isAlkuPvmInFuture && (
                <div className={styles.DeleteBtn}>
                    <Button color="danger" onClick={handleDeleteClick}>
                        {i18n.translate('POISTA_AJASTETTU_NIMENMUUTOS')}
                    </Button>
                </div>
            )}
        </div>
    );
}
