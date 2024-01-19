import React from 'react';
import styles from './ViestiStatusView.module.css';
import { ModalishBox } from './ModalishBox';
import Button from '@opetushallitus/virkailija-ui-components/Button';

export function GenericOsoitepalveluError() {
    return (
        <ModalishBox className={styles.Error}>
            <h1>Osoitepalvelu ei vastaa</h1>
            <p>Emme saaneet yhteyttä osoitepalvellun</p>
            <div className={styles.ButtonRow}>
                <Button onClick={reloadPage}>Yritä uudelleen</Button>
            </div>
        </ModalishBox>
    );
}

function reloadPage() {
    window.location.reload();
}
