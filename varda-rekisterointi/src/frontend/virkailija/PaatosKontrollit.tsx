import React, {useContext, useEffect, useState} from "react";
import {LanguageContext} from '../contexts';
import Button from "@opetushallitus/virkailija-ui-components/Button";
import PaatosVahvistus from "./PaatosVahvistus";

type Props = {
    valitut: number[]
    tyhjennaValinnatCallback: () => void
}

export default function PaatosKontrollit({ valitut, tyhjennaValinnatCallback }: Props) {
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
            <Button disabled={!kaytossa} onClick={_ => vahvista(false)}>
                {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
            </Button>
            <Button disabled={!kaytossa} onClick={_ => vahvista(true)}>
                {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
            </Button>
            <PaatosVahvistus valitut={valitut}
                             hyvaksytty={hyvaksytty}
                             nayta={naytaVahvistus}
                             tyhjennaValinnatCallback={tyhjennaValinnatCallback}
                             suljeCallback={() => asetaNaytaVahvistus(false)}/>
        </div>
    );
}
