import React, { useContext, useState } from 'react';
import Axios from 'axios';
import { LanguageContext, MaatJaValtiotKoodistoContext, useKoodistoContext, useModalContext } from '../../../contexts';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import Button from '@opetushallitus/virkailija-ui-components/Button';

import styles from './ApprovalModal.module.css';

import Modal from '@opetushallitus/virkailija-ui-components/Modal';
import ModalBody from '@opetushallitus/virkailija-ui-components/ModalBody';
import ModalFooter from '@opetushallitus/virkailija-ui-components/ModalFooter';
import ModalHeader from '@opetushallitus/virkailija-ui-components/ModalHeader';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import { isNonEmpty } from '../../../StringUtils';
import { ApprovalCallback, Organisaatio } from '../../../types/types';

const paatoksetBatchUrl = '/varda-rekisterointi/virkailija/api/paatokset/batch';

type PaatosBatch = {
    hyvaksytty: boolean;
    hakemukset: number[];
    perustelu?: string;
};

type Props = {
    chosenRegistrations: Rekisterointihakemus[];
    approvalDecision: boolean;
    approvalCallback: ApprovalCallback;
};

class PaatosRivi {
    constructor(readonly hakemus: Rekisterointihakemus, readonly kotipaikka: string) {}

    get organisaatio(): string {
        return this.hakemus.organisaatio.ytjNimi.nimi;
    }

    get puhelinnumero(): string {
        return this.hakemus.organisaatio.yhteystiedot.puhelinnumero || '';
    }

    get ytunnus(): string {
        return this.hakemus.organisaatio.ytunnus;
    }
}

export default function ApprovalModal({ chosenRegistrations, approvalDecision, approvalCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const { kunnat: kuntaKoodisto } = useKoodistoContext();
    const { koodisto: maatJaValtiotKoodisto } = useContext(MaatJaValtiotKoodistoContext);
    const [perustelu, asetaPerustelu] = useState<string>();
    const [perusteluError, setPerusteluError] = useState(false);
    const [lahetaError, setLahetaError] = useState(false);
    const { closeModal } = useModalContext();

    async function laheta() {
        setPerusteluError(false);
        const paatokset: PaatosBatch = {
            hyvaksytty: approvalDecision,
            hakemukset: chosenRegistrations.map((h) => h.id),
        };
        if (isNonEmpty(perustelu)) {
            paatokset.perustelu = perustelu;
        } else if (!approvalDecision) {
            return setPerusteluError(true);
        }
        try {
            await Axios.post(paatoksetBatchUrl, paatokset);
            approvalCallback(chosenRegistrations, approvalDecision, perustelu);
            closeModal();
        } catch (e) {
            setLahetaError(true);
            throw e;
        }
    }

    function kotipaikka(organisaatio: Organisaatio): string {
        const osat: string[] = [];
        const kunta = kuntaKoodisto.uri2Nimi(organisaatio.kotipaikkaUri);
        const maa = maatJaValtiotKoodisto.uri2Nimi(organisaatio.maaUri);
        if (kunta) osat.push(kunta);
        if (maa) osat.push(maa);
        return osat.join(', ');
    }

    return (
        <Modal open onClose={closeModal} className={styles.modalContainer}>
            <ModalHeader onClose={closeModal} className={styles.modalHeader}>
                {i18n.translate(approvalDecision ? 'REKISTEROINNIT_HYVAKSYTTAVAT' : 'REKISTEROINNIT_HYLATTAVAT')}
            </ModalHeader>
            <ModalBody className={styles.modalBody}>
                <div className={styles.tableContainer}>
                    <table className={styles.paatosLista}>
                        <thead>
                            <tr>
                                <th className={styles.organizationNameCell}>{i18n.translate('ORGANISAATION_NIMI')}</th>
                                <th>{i18n.translate('PUHELINNUMERO')}</th>
                                <th>{i18n.translate('YTUNNUS')}</th>
                                <th>{i18n.translate('KOTIPAIKKA')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {chosenRegistrations
                                .map((hakemus) => new PaatosRivi(hakemus, `${kotipaikka(hakemus.organisaatio)}`))
                                .map((rivi) => (
                                    <tr key={rivi.hakemus.id}>
                                        <td className={styles.organizationNameCell}>{rivi.organisaatio}</td>
                                        <td>{rivi.puhelinnumero}</td>
                                        <td>{rivi.ytunnus}</td>
                                        <td>{rivi.kotipaikka}</td>
                                    </tr>
                                ))}
                        </tbody>
                    </table>
                </div>
                {!approvalDecision && (
                    <div>
                        <p className={styles.hylkaysOhje}>{i18n.translate('REKISTEROINTI_HYLKAYS_OHJE')}</p>
                        {chosenRegistrations.length > 1 ? (
                            <p>{i18n.translate('REKISTEROINTI_HYLKAYS_MONTAVALITTUNA')}</p>
                        ) : null}
                        <Textarea
                            error={perusteluError}
                            value={perustelu}
                            onChange={(event: { target: HTMLTextAreaElement }) => asetaPerustelu(event.target.value)}
                        />
                    </div>
                )}
            </ModalBody>
            <ModalFooter className={styles.footer}>
                <div>
                    {lahetaError ? (
                        <div className={` ${styles.virheLomakkeella} ${styles.virheAsettelu}`}>
                            {i18n.translate('ERROR_SAVE')}
                        </div>
                    ) : null}
                </div>
                <div className={styles.footerButtons}>
                    <Button variant="text" onClick={closeModal}>
                        {i18n.translate('PERUUTA')}
                    </Button>
                    <Button onClick={laheta}>
                        {i18n.translate(approvalDecision ? 'TAULUKKO_HYVAKSY_HAKEMUS' : 'TAULUKKO_HYLKAA_HAKEMUS')}
                    </Button>
                </div>
            </ModalFooter>
        </Modal>
    );
}
