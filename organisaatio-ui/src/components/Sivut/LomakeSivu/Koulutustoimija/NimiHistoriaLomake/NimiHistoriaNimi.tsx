import * as React from 'react';
import { useContext } from 'react';
import { LanguageContext } from '../../../../../contexts/LanguageContext';
import { LocalDate, Nimi, OrganisaationNimetNimi } from '../../../../../types/types';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import styles from './NimiHistoriaNimi.module.css';
import { ReadOnlyNimi } from '../../LomakeFields/LomakeFields';

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

    const isAlkuPvmInFuture = new Date(alkuPvm) > new Date();
    return (
        <div className={styles.NimiHistoriaCell}>
            <ReadOnlyNimi value={nimi} />
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
