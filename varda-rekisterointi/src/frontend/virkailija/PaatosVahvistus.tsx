import React, {useContext, useState} from "react";
import Axios from "axios";
import {KuntaKoodistoContext, LanguageContext, OpetuskieliKoodistoContext} from '../contexts';
import Box from "@opetushallitus/virkailija-ui-components/Box";
import Button from "@opetushallitus/virkailija-ui-components/Button";
import Modal from "@opetushallitus/virkailija-ui-components/Modal"
import ModalBody from "@opetushallitus/virkailija-ui-components/ModalBody"
import ModalFooter from "@opetushallitus/virkailija-ui-components/ModalFooter"
import ModalHeader from "@opetushallitus/virkailija-ui-components/ModalHeader"
import {Rekisterointihakemus} from "./rekisterointihakemus";
import {hasLength} from "../StringUtils";

const paatoksetBatchUrl = "/varda-rekisterointi/virkailija/api/paatokset/batch";

type PaatosBatch = {
    hyvaksytty: boolean
    hakemukset: number[],
    perustelu?: string
}

type Props = {
    valitut: Rekisterointihakemus[]
    hyvaksytty: boolean
    nayta: boolean
    valitutKasiteltyCallback: () => void
    suljeCallback: () => void
}

class PaatosRivi {

    constructor(readonly hakemus: Rekisterointihakemus, readonly kotipaikka: string, readonly opetuskieli: string) {}

    get organisaatio(): string {
        return this.hakemus.organisaatio.ytjNimi.nimi;
    }

    get vastuuhenkilo(): string {
        return `${this.hakemus.kayttaja.etunimi} ${this.hakemus.kayttaja.sukunimi}`
    }

    get ytunnus(): string {
        return this.hakemus.organisaatio.ytunnus;
    }

}

export default function PaatosVahvistus({ valitut, hyvaksytty, nayta, valitutKasiteltyCallback, suljeCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const { koodisto: kuntaKoodisto } = useContext(KuntaKoodistoContext);
    const { koodisto: opetuskieliKoodisto } = useContext(OpetuskieliKoodistoContext);
    const [ perustelu, asetaPerustelu ] = useState("");

    async function laheta() {
        const paatokset: PaatosBatch = {
            hyvaksytty,
            hakemukset: valitut.map(h => h.id)
        };
        if (hasLength(perustelu)) {
            paatokset.perustelu = perustelu;
        }
        try {
            await Axios.post(paatoksetBatchUrl, paatokset);
            valitutKasiteltyCallback();
            suljeCallback();
        } catch (e) {
            // TODO: virheenkäsittely
            console.log(e);
        }
    }

    function opetuskielet(kieliUris: string[]): string {
        if (!kieliUris || kieliUris.length === 0) return "";
        return kieliUris.filter(
            kieliUri => kieliUri.startsWith("oppilaitoksenopetuskieli_")
        ).map(
            kieliUri => opetuskieliKoodisto.uri2Nimi(kieliUri)
        ).join(", ");
    }

    return (
        <Modal open={nayta} onClose={suljeCallback}>
            <ModalHeader onClose={suljeCallback}>{i18n.translate(hyvaksytty ? 'REKISTEROINNIT_HYVAKSYTTAVAT' : 'REKISTEROINNIT_HYLATTAVAT')}</ModalHeader>
            <ModalBody>
                <table>
                    <thead>
                        <tr key="otsikot">
                            <th>{i18n.translate('ORGANISAATION_NIMI')}</th>
                            <th>{i18n.translate('VASTUUHENKILO')}</th>
                            <th>{i18n.translate('YTUNNUS')}</th>
                            <th>{i18n.translate('ORGANISAATION_KOTIPAIKKA')}</th>
                            <th>{i18n.translate('OPETUSKIELI')}</th>
                        </tr>
                    </thead>
                    <tbody>
                    {
                        valitut.map(hakemus => new PaatosRivi(
                            hakemus,
                            `${kuntaKoodisto.uri2Nimi(hakemus.organisaatio.kotipaikkaUri)}`,
                            opetuskielet(hakemus.organisaatio.kieletUris)
                        )).map(rivi =>
                        <tr key={rivi.hakemus.id}>
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
            { hyvaksytty ? null :
                <textarea value={perustelu}
                          onChange={(event) => asetaPerustelu(event.currentTarget.value)} />
            }
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
