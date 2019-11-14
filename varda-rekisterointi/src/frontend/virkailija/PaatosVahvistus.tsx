import React, {useContext} from "react";
import Axios from "axios";
import {LanguageContext} from '../contexts';
import Box from "@opetushallitus/virkailija-ui-components/Box";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Modal from "@opetushallitus/virkailija-ui-components/Modal"
import ModalBody from "@opetushallitus/virkailija-ui-components/ModalBody"
import ModalFooter from "@opetushallitus/virkailija-ui-components/ModalFooter"
import ModalHeader from "@opetushallitus/virkailija-ui-components/ModalHeader"
import {Rekisterointihakemus} from "./rekisterointihakemus";

const paatoksetBatchUrl = "/varda-rekisterointi/virkailija/api/paatokset/batch";

type PaatosBatch = {
    hyvaksytty: boolean
    hakemukset: number[]
}

type Props = {
    valitut: Rekisterointihakemus[]
    hyvaksytty: boolean
    nayta: boolean
    tyhjennaValinnatCallback: () => void
    suljeCallback: () => void
}

export default function PaatosVahvistus({ valitut, hyvaksytty, nayta, tyhjennaValinnatCallback, suljeCallback }: Props) {
    const { i18n } = useContext(LanguageContext);

    async function laheta() {
        const paatokset: PaatosBatch = {
            hyvaksytty,
            hakemukset: valitut.map(h => h.id)
        };
        try {
            await Axios.post(paatoksetBatchUrl, paatokset);
            tyhjennaValinnatCallback();
            suljeCallback();
        } catch (e) {
            // TODO: virheenkäsittely
            console.log(e);
        }
    }

    return (
        <Modal open={nayta} onClose={suljeCallback}>
            <ModalHeader onClose={suljeCallback}>{i18n.translate(hyvaksytty ? 'REKISTEROINNIT_HYVAKSYTTAVAT' : 'REKISTEROINNIT_HYLATTAVAT')}</ModalHeader>
            <ModalBody>
                Blabla!
            </ModalBody>
            <ModalFooter>
                <Box display="flex" justifyContent="flex-end">
                    <Button variant="text" onClick={suljeCallback}>{i18n.translate('REKISTEROINTI_PERUUTA')}</Button>
                    <Button onClick={laheta}>{i18n.translate('REKISTEROINTI_LAHETA')}</Button>
                </Box>
            </ModalFooter>
        </Modal>
    );
}
