import React, { useContext } from 'react';
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

function ClearIcon() {
    return (
        <svg aria-hidden="true" focusable="false" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
        </svg>
    );
}

function CheckOutlinedIcon() {
    return (
        <svg aria-hidden="true" focusable="false" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor">
            <path d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
        </svg>
    );
}

export default function ApprovalButtonsContainer({ chosenRekisteroinnit, approvalCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const { setModal } = useModalContext();
    const chosen = chosenRekisteroinnit.length;
    const buttonsInUse = chosen > 0;

    function confirmApprovalSelection(hyvaksytty: boolean) {
        setModal(
            <ApprovalModal
                chosenRegistrations={chosenRekisteroinnit}
                approvalDecision={hyvaksytty}
                approvalCallback={approvalCallback}
            />
        );
    }

    return (
        <Box className={styles.approvalButtonsContainer}>
            <Button
                disabled={!buttonsInUse}
                onClick={() => confirmApprovalSelection(false)}
                variant="outlined"
                color="secondary"
            >
                <ClearIcon /> {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
                {chosen ? ` (${chosen})` : ''}
            </Button>
            <Button disabled={!buttonsInUse} onClick={() => confirmApprovalSelection(true)}>
                <CheckOutlinedIcon /> {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
                {chosen ? ` (${chosen})` : ''}
            </Button>
        </Box>
    );
}
