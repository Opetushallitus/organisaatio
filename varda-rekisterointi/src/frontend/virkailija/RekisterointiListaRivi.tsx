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

    get vastuuhenkilo(): string {
        return `${this.hakemus.kayttaja.etunimi} ${this.hakemus.kayttaja.sukunimi}`
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
    rekisterointi: ListaRivi
    riviValittu: boolean
    valitseHakemusCallback: (id: number, valittu: boolean) => void
}

export default function RekisterointiListaRivi({ rekisterointi, riviValittu, valitseHakemusCallback } : Props) {
    const [valittu, asetaValittu] = useState(false);

    useEffect(() => {
        asetaValittu(riviValittu);
    }, [riviValittu]);

    function valitse() {
        asetaValittu(vanhaTila => !vanhaTila);
        valitseHakemusCallback(rekisterointi.hakemus.id, valittu);
    }

    return (
        <tr>
            <td><Checkbox checked={valittu} onChange={_ => valitse()} /></td>
            <td>{rekisterointi.organisaatio}</td>
            <td>{rekisterointi.vastuuhenkilo}</td>
            <td>{rekisterointi.ytunnus}</td>
            <td>{rekisterointi.vastaanotettu}</td>
        </tr>
    )
}
