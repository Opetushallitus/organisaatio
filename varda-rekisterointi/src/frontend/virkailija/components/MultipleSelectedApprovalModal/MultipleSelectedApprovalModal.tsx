import React, { useContext, useState } from 'react';
import Axios from 'axios';
import { LanguageContext, MaatJaValtiotKoodistoContext, useKoodistoContext } from '../../../contexts';
import Box from '@opetushallitus/virkailija-ui-components/Box';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import Typography from '@opetushallitus/virkailija-ui-components/Typography';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Modal from '@opetushallitus/virkailija-ui-components/Modal';
import ModalBody from '@opetushallitus/virkailija-ui-components/ModalBody';
import ModalFooter from '@opetushallitus/virkailija-ui-components/ModalFooter';
import ModalHeader from '@opetushallitus/virkailija-ui-components/ModalHeader';
import Divider from '@opetushallitus/virkailija-ui-components/Divider';
import { Rekisterointihakemus } from '../../rekisterointihakemus';
import { isNonEmpty } from '../../../StringUtils';
import { Organisaatio } from '../../../types/types';
import styles from './MultipleSelectedApprovalModal.module.css';

const paatoksetBatchUrl = '/varda-rekisterointi/virkailija/api/paatokset/batch';

type PaatosBatch = {
    hyvaksytty: boolean;
    hakemukset: number[];
    perustelu?: string;
};

type Props = {
    chosenRegistrations: Rekisterointihakemus[];
    approvalDecision: boolean;
    modalOpen: boolean;
    approvalDoneCb: (hyvaksytty: boolean) => void;
    closeButtonCb: () => void;
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

export default function MultipleSelectedApprovalModal({
    chosenRegistrations,
    approvalDecision,
    modalOpen,
    approvalDoneCb,
    closeButtonCb,
}: Props) {
    const { i18n } = useContext(LanguageContext);
    const { kunnat: kuntaKoodisto } = useKoodistoContext();
    const { koodisto: maatJaValtiotKoodisto } = useContext(MaatJaValtiotKoodistoContext);
    const [perustelu, asetaPerustelu] = useState('');
    const [perusteluError, setPerusteluError] = useState(false);
    const [lahetaError, setLahetaError] = useState(false);

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
            approvalDoneCb(approvalDecision);
            closeButtonCb();
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
        <Modal open={modalOpen} onClose={closeButtonCb}>
            <ModalHeader onClose={closeButtonCb}>
                {i18n.translate(approvalDecision ? 'REKISTEROINNIT_HYVAKSYTTAVAT' : 'REKISTEROINNIT_HYLATTAVAT')}
            </ModalHeader>
            <ModalBody>
                <table className={styles.paatosLista}>
                    <thead>
                        <tr key="otsikot">
                            <th>{i18n.translate('ORGANISAATION_NIMI')}</th>
                            <th>{i18n.translate('PUHELINNUMERO')}</th>
                            <th>{i18n.translate('YTUNNUS')}</th>
                            <th>{i18n.translate('ORGANISAATION_KOTIPAIKKA')}</th>
                        </tr>
                    </thead>
                    <tbody>
                        {chosenRegistrations
                            .map((hakemus) => new PaatosRivi(hakemus, `${kotipaikka(hakemus.organisaatio)}`))
                            .map((rivi) => (
                                <tr key={rivi.hakemus.id}>
                                    <td>{rivi.organisaatio}</td>
                                    <td>{rivi.puhelinnumero}</td>
                                    <td>{rivi.ytunnus}</td>
                                    <td>{rivi.kotipaikka}</td>
                                </tr>
                            ))}
                    </tbody>
                </table>
                {!approvalDecision && [
                    chosenRegistrations.length > 1
                        ? [
                              <Divider />,
                              <Typography>{i18n.translate('REKISTEROINTI_HYLKAYS_MONTAVALITTUNA')}</Typography>,
                              <Divider />,
                          ]
                        : null,
                    <Typography className={perusteluError ? styles.virheLomakkeella : ''}>
                        {i18n.translate('REKISTEROINTI_HYLKAYS_OHJE')}
                    </Typography>,
                    <Textarea
                        className={styles.lisattyMargin}
                        error={perusteluError}
                        value={perustelu}
                        onChange={(event: { target: HTMLTextAreaElement }) => asetaPerustelu(event.target.value)}
                    />,
                ]}
            </ModalBody>
            <ModalFooter>
                <Box display="flex" justifyContent="flex-end">
                    {lahetaError ? (
                        <div className={` ${styles.virheLomakkeella} ${styles.virheAsettelu}`}>
                            {i18n.translate('ERROR_SAVE')}
                        </div>
                    ) : null}
                    <Button variant="text" onClick={closeButtonCb}>
                        {i18n.translate('REKISTEROINTI_PERUUTA')}
                    </Button>
                    <Button onClick={laheta}>{i18n.translate('REKISTEROINTI_LAHETA')}</Button>
                </Box>
            </ModalFooter>
        </Modal>
    );
}
