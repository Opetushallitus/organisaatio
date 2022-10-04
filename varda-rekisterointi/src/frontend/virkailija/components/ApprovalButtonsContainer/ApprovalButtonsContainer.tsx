import React, { useContext, useEffect, useState } from 'react';
import Clear from '@material-ui/icons/Clear';
import { CheckOutlined } from '@material-ui/icons';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Box from '@opetushallitus/virkailija-ui-components/Box';

import { LanguageContext, useModalContext } from '../../../contexts';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import ApprovalModal from '../ApprovalModal/ApprovalModal';
import { ApprovalCallback } from '../../../types/types';

import styles from './ApprovalButtonsContainer.module.css';

type Props = {
    chosenRekisteroinnit: Rekisterointihakemus[];
    approvalCallback: ApprovalCallback;
};

export default function ApprovalButtonsContainer({ chosenRekisteroinnit, approvalCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [buttonsInUse, setButtonsInUse] = useState(false);
    const { setModal } = useModalContext();
    const chosen = chosenRekisteroinnit.length;

    function confirmApprovalSelection(hyvaksytty: boolean) {
        setModal(
            <ApprovalModal
                chosenRegistrations={chosenRekisteroinnit}
                approvalDecision={hyvaksytty}
                approvalCallback={approvalCallback}
            />
        );
    }

    useEffect(() => {
        setButtonsInUse(chosen > 0);
    }, [chosen]);

    return (
        <Box className={styles.approvalButtonsContainer}>
            <Button
                disabled={!buttonsInUse}
                onClick={() => confirmApprovalSelection(false)}
                variant="outlined"
                color="secondary"
            >
                <Clear /> {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
                {chosen ? ` (${chosen})` : ''}
            </Button>
            <Button disabled={!buttonsInUse} onClick={() => confirmApprovalSelection(true)}>
                <CheckOutlined /> {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
                {chosen ? ` (${chosen})` : ''}
            </Button>
        </Box>
    );
}
