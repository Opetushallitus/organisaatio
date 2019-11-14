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

class PaatosRivi {

    constructor(readonly hakemus: Rekisterointihakemus) {}

    get organisaatio(): string {
        return this.hakemus.organisaatio.ytjNimi.nimi;
    }

    get vastuuhenkilo(): string {
        return `${this.hakemus.kayttaja.etunimi} ${this.hakemus.kayttaja.sukunimi}`
    }

    get ytunnus(): string {
        return this.hakemus.organisaatio.ytunnus;
    }

    get kotipaikka(): string {
        // TODO: uri -> kunta/maa
        return `${this.hakemus.organisaatio.kotipaikkaUri}, ${this.hakemus.organisaatio.maaUri}`;
    }

    get opetuskieli(): string {
        // TODO: monta opetuskieltä? kieliuri -> kieli
        return "Suomi";
    }
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
                <table>
                    <thead>
                        <tr>
                            <th>{i18n.translate('ORGANISAATION_NIMI')}</th>
                            <th>{i18n.translate('VASTUUHENKILO')}</th>
                            <th>{i18n.translate('YTUNNUS')}</th>
                            <th>{i18n.translate('ORGANISAATION_KOTIPAIKKA')}</th>
                            <th>{i18n.translate('OPETUSKIELI')}</th>
                        </tr>
                    </thead>
                    <tbody>
                    {
                        valitut.map(hakemus => new PaatosRivi(hakemus)).map(rivi =>
                        <tr>
                            <td>{rivi.organisaatio}</td>
                            <td>{rivi.vastuuhenkilo}</td>
                            <td>{rivi.ytunnus}</td>
                            <td>{rivi.kotipaikka}</td>
                            <td>{rivi.opetuskieli}</td>
                        </tr>
                        )
                    }
                    </tbody>
                </table>
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
