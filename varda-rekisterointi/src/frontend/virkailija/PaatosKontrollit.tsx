import React, {useContext, useEffect, useState} from "react";
import {LanguageContext} from '../contexts';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import PaatosVahvistus from "./PaatosVahvistus";
import {Rekisterointihakemus} from "./rekisterointihakemus";

type Props = {
    valitut: Rekisterointihakemus[]
    valitutKasiteltyCallback: () => void
}

export default function PaatosKontrollit({ valitut, valitutKasiteltyCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [kaytossa, asetaKaytossa] = useState(false);
    const [hyvaksytty, asetaHyvaksytty] = useState(false);
    const [naytaVahvistus, asetaNaytaVahvistus] = useState(false);

    function vahvista(hyvaksytty: boolean) {
        asetaHyvaksytty(hyvaksytty);
        asetaNaytaVahvistus(true);
    }

    useEffect(() => {
        asetaKaytossa(valitut && valitut.length > 0);
    }, [valitut]);

    return (
        <div>
            <Button id="hylkaaButton" disabled={!kaytossa} onClick={_ => vahvista(false)}>
                {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
            </Button>
            <Button id="hyvaksyButton" disabled={!kaytossa} onClick={_ => vahvista(true)}>
                {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
            </Button>
            <PaatosVahvistus valitut={valitut}
                             hyvaksytty={hyvaksytty}
                             nayta={naytaVahvistus}
                             valitutKasiteltyCallback={valitutKasiteltyCallback}
                             suljeCallback={() => asetaNaytaVahvistus(false)}/>
        </div>
    );
}
