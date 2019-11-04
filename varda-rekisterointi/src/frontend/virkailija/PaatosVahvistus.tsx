import React, {useContext} from "react";
import Axios from "axios";
import {LanguageContext} from '../contexts';
//import Button from "@opetushallitus/virkailija-ui-components/Button";
import Modal from "@opetushallitus/virkailija-ui-components/Modal"
import ModalBody from "@opetushallitus/virkailija-ui-components/ModalBody"
import ModalFooter from "@opetushallitus/virkailija-ui-components/ModalFooter"
import ModalHeader from "@opetushallitus/virkailija-ui-components/ModalHeader"

const paatoksetBatchUrl = "/varda-rekisterointi/virkailija/api/paatokset/batch";

type PaatosBatch = {
    hyvaksytty: boolean
    hakemukset: number[]
}

type Props = {
    valitut: number[]
    hyvaksy: boolean
    nayta: boolean
    tyhjennaValinnatCallback: () => void
    suljeCallback: () => void
}

export default function PaatosVahvistus({ valitut, hyvaksy, nayta, tyhjennaValinnatCallback, suljeCallback }: Props) {
    const { i18n } = useContext(LanguageContext);

    async function laheta() {
        const paatokset = {
            hyvaksy,
            hakemukset: valitut
        };
        try {
            const response = await Axios.post(paatoksetBatchUrl, paatokset);
            tyhjennaValinnatCallback();
        } catch (e) {
            // TODO: virheenkäsittely
            console.log(e);
        }
    }

    return (
        <Modal open={nayta} onClose={suljeCallback}>
            <ModalHeader onClose={suljeCallback}>{i18n.translate(hyvaksy ? 'REKISTEROINNIT_HYVAKSYTTAVAT' : 'REKISTEROINNIT_HYLATTAVAT')}</ModalHeader>
            <ModalBody>
                Blabla!
            </ModalBody>
            <ModalFooter>
                Yadda yadda!
            </ModalFooter>
        </Modal>
    );
}
