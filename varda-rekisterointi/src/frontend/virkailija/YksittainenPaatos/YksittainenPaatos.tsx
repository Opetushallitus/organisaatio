import React, { useContext } from 'react';
import { LanguageContext } from '../../contexts';
import Box from '@opetushallitus/virkailija-ui-components/Box';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Modal from '@opetushallitus/virkailija-ui-components/Modal';
import ModalBody from '@opetushallitus/virkailija-ui-components/ModalBody';
import ModalFooter from '@opetushallitus/virkailija-ui-components/ModalFooter';

import { Rekisterointi } from '../../types/types';
import Fieldset from '../../Fieldset';
import OrganisaationTiedot from './OrganisaationTiedot';
import OrgYhteystiedot from './OrgYhteystiedot';

import styles from '../components/ApprovalButtonsContainer/PaatosKontrollit.module.css';
import ModalHeader from '@opetushallitus/virkailija-ui-components/ModalHeader';

type Props = {
    valittu: Rekisterointi;
    suljeCallback: () => void;
};

export default function YksittainenPaatos({ valittu, suljeCallback }: Props) {
    const { i18n } = useContext(LanguageContext);

    return (
        <Modal maxWidth={'80%'} open onClose={suljeCallback}>
            <ModalHeader onClose={suljeCallback}>{i18n.translate('REKISTEROINNIT_INFO')}</ModalHeader>
            <ModalBody>
                <div className="varda-rekisterointi-hakija">
                    <Fieldset title={i18n.translate('ORGANISAATION_TIEDOT')}>
                        <OrganisaationTiedot
                            organisaatio={valittu.organisaatio}
                            toimintamuoto={valittu.toimintamuoto}
                            kunnat={valittu.kunnat}
                        />
                    </Fieldset>
                    <Fieldset title={i18n.translate('ORGANISAATION_YHTEYSTIEDOT')}>
                        <OrgYhteystiedot yhteystiedot={valittu.organisaatio.yhteystiedot} />
                    </Fieldset>
                </div>
            </ModalBody>
            <ModalFooter>
                <Box display="flex" justifyContent="flex-end">
                    <Button
                        color={'danger'}
                        variant={'outlined'}
                        onClick={suljeCallback}
                        className={styles.paatosKontrollit}
                    >
                        <i className="material-icons md-18">&#xe14c;</i>
                        {i18n.translate('REKISTEROINNIT_INFO_SULJE')}
                    </Button>
                </Box>
            </ModalFooter>
        </Modal>
    );
}
