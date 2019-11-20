import React, {useEffect, useState} from "react";
import {format, parseISO} from "date-fns";
import {Rekisterointihakemus} from "./rekisterointihakemus";

import Checkbox from "@opetushallitus/virkailija-ui-components/Checkbox";

const saapumisAikaFormat = 'd.M.y HH:mm';

export class ListaRivi {

    constructor(readonly hakemus: Rekisterointihakemus) {}

    get organisaatio(): string {
        return this.hakemus.organisaatio.ytjNimi.nimi;
    }

    get sahkoposti(): string {
        return this.hakemus.sahkopostit[0];
    }

    get ytunnus(): string {
        return this.hakemus.organisaatio.ytunnus;
    }

    get vastaanotettu(): string {
        return this.hakemus.vastaanotettu
            ? format(parseISO(this.hakemus.vastaanotettu), saapumisAikaFormat)
            : ""
    }

}

type Props = {
    valintaKaytossa: boolean
    rekisterointi: ListaRivi
    riviValittu: boolean
    valitseHakemusCallback: (hakemus: Rekisterointihakemus, valittu: boolean) => void
}

export default function RekisterointiListaRivi({ valintaKaytossa, rekisterointi, riviValittu, valitseHakemusCallback } : Props) {
    const [valittu, asetaValittu] = useState(false);

    useEffect(() => {
        asetaValittu(riviValittu);
    }, [valintaKaytossa, riviValittu]);

    function valitse() {
        asetaValittu(vanhaTila => !vanhaTila);
        valitseHakemusCallback(rekisterointi.hakemus, valittu);
    }

    return (
        <tr>
        {
            valintaKaytossa &&
            <td><Checkbox checked={valittu} onChange={_ => valitse()} /></td>
        }
            <td>{rekisterointi.organisaatio}</td>
            <td>{rekisterointi.sahkoposti}</td>
            <td>{rekisterointi.ytunnus}</td>
            <td>{rekisterointi.vastaanotettu}</td>
        </tr>
    )
}
