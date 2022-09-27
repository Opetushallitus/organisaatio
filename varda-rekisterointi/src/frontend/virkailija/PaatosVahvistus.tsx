import React, { useContext, useState } from 'react';
import Axios from 'axios';
import { KuntaKoodistoContext, LanguageContext, MaatJaValtiotKoodistoContext } from '../contexts';
import Box from '@opetushallitus/virkailija-ui-components/Box';
import Textarea from '@opetushallitus/virkailija-ui-components/Textarea';
import Typography from '@opetushallitus/virkailija-ui-components/Typography';
import Button from '@opetushallitus/virkailija-ui-components/Button';
import Modal from '@opetushallitus/virkailija-ui-components/Modal';
import ModalBody from '@opetushallitus/virkailija-ui-components/ModalBody';
import ModalFooter from '@opetushallitus/virkailija-ui-components/ModalFooter';
import ModalHeader from '@opetushallitus/virkailija-ui-components/ModalHeader';
import Divider from '@opetushallitus/virkailija-ui-components/Divider';
import { Rekisterointihakemus } from './rekisterointihakemus';
import { isNonEmpty } from '../StringUtils';
import { Organisaatio } from '../types/types';
import styles from './PaatosVahvistus.module.css';
import { Row } from '@tanstack/react-table';

const paatoksetBatchUrl = '/varda-rekisterointi/virkailija/api/paatokset/batch';

type PaatosBatch = {
    hyvaksytty: boolean;
    hakemukset: number[];
    perustelu?: string;
};

type Props = {
    valitut: Rekisterointihakemus[];
    hyvaksytty: boolean;
    nayta: boolean;
    valitutKasiteltyCallback: (hyvaksytty: boolean) => void;
    suljeCallback: () => void;
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

export default function PaatosVahvistus({
    valitut,
    hyvaksytty,
    nayta,
    valitutKasiteltyCallback,
    suljeCallback,
}: Props) {
    const { i18n } = useContext(LanguageContext);
    const { koodisto: kuntaKoodisto } = useContext(KuntaKoodistoContext);
    const { koodisto: maatJaValtiotKoodisto } = useContext(MaatJaValtiotKoodistoContext);
    const [perustelu, asetaPerustelu] = useState('');
    const [perusteluError, setPerusteluError] = useState(false);
    const [lahetaError, setLahetaError] = useState(false);

    async function laheta() {
        setPerusteluError(false);
        const paatokset: PaatosBatch = {
            hyvaksytty,
            hakemukset: valitut.map((h) => h.id),
        };
        if (isNonEmpty(perustelu)) {
            paatokset.perustelu = perustelu;
        } else if (!hyvaksytty) {
            return setPerusteluError(true);
        }
        try {
            await Axios.post(paatoksetBatchUrl, paatokset);
            valitutKasiteltyCallback(hyvaksytty);
            suljeCallback();
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
        <Modal open={nayta} onClose={suljeCallback}>
            <ModalHeader onClose={suljeCallback}>
                {i18n.translate(hyvaksytty ? 'REKISTEROINNIT_HYVAKSYTTAVAT' : 'REKISTEROINNIT_HYLATTAVAT')}
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
                        {valitut
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
                {!hyvaksytty && [
                    valitut.length > 1
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
                    <Button variant="text" onClick={suljeCallback}>
                        {i18n.translate('REKISTEROINTI_PERUUTA')}
                    </Button>
                    <Button onClick={laheta}>{i18n.translate('REKISTEROINTI_LAHETA')}</Button>
                </Box>
            </ModalFooter>
        </Modal>
    );
}
