import React, { useContext, useEffect, useState } from 'react';
import { LanguageContext, useModalContext } from '../../../contexts';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import Box from '@opetushallitus/virkailija-ui-components/Box';
import styles from './ApprovalButtonsContainer.module.css';
import MultipleSelectedApprovalModal from '../MultipleSelectedApprovalModal/MultipleSelectedApprovalModal';

type Props = {
    chosenRekisteroinnit: Rekisterointihakemus[];
    valitutKasiteltyCallback: (hyvaksytty: boolean) => void;
};

export default function ApprovalButtonsContainer({ chosenRekisteroinnit, valitutKasiteltyCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [buttonsInUse, setButtonsInUse] = useState(false);
    const { setModal } = useModalContext();

    function confirmApprovalSelection(hyvaksytty: boolean) {
        setModal(
            <MultipleSelectedApprovalModal
                chosenRegistrations={chosenRekisteroinnit}
                approvalDecision={hyvaksytty}
                approvalDoneCb={valitutKasiteltyCallback}
            />
        );
    }

    useEffect(() => {
        setButtonsInUse(chosenRekisteroinnit && chosenRekisteroinnit.length > 0);
    }, [chosenRekisteroinnit]);

    return (
        <Box className={styles.approvalButtonsContainer}>
            <Button
                disabled={!buttonsInUse}
                onClick={() => confirmApprovalSelection(false)}
                variant="outlined"
                color="secondary"
            >
                <i className="material-icons md-18">&#xe14c;</i> {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
            </Button>
            <Button disabled={!buttonsInUse} onClick={() => confirmApprovalSelection(true)}>
                <i className="material-icons md-18">&#xe5ca;</i> {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
            </Button>
        </Box>
    );
}
