import React from 'react';
import Modal from '@opetushallitus/virkailija-ui-components/Modal';
import ModalBody from '@opetushallitus/virkailija-ui-components/ModalBody';
import ModalFooter from '@opetushallitus/virkailija-ui-components/ModalFooter';

import styles from './PohjaModaali.module.css';
import ModalHeader from '@opetushallitus/virkailija-ui-components/ModalHeader';

type Props = {
    suljeCallback?: () => void;
    header?: React.ReactNode;
    body?: React.ReactNode;
    footer?: React.ReactNode;
};

export default function PohjaModaali({ suljeCallback = () => {}, footer, header, body }: Props) {
    return (
        <Modal className={styles.ModaaliKehys} open onClose={suljeCallback}>
            <ModalHeader className={styles.ModaaliOtsikkoKehys} onClose={suljeCallback}>
                {header}
            </ModalHeader>
            <ModalBody>{body}</ModalBody>
            <ModalFooter className={styles.ModaaliFooterKehys}>{footer}</ModalFooter>
        </Modal>
    );
}
