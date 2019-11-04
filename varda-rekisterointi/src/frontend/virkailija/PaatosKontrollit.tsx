import React, {useContext, useEffect, useState} from "react";
import Axios from "axios";
import {LanguageContext} from '../contexts';
import Button from "@opetushallitus/virkailija-ui-components/Button";

const paatoksetBatchUrl = "/varda-rekisterointi/virkailija/api/paatokset/batch";

type PaatosBatch = {
    hyvaksytty: boolean
    hakemukset: number[]
}

type Props = {
    valitut: number[]
    tyhjennaValinnatCallback: () => void
}

export default function PaatosKontrollit({ valitut, tyhjennaValinnatCallback }: Props) {
    const { i18n } = useContext(LanguageContext);
    const [kaytossa, asetaKaytossa] = useState(false);

    function paatos(hyvaksytty: boolean) {
        const batch = {
            hyvaksytty,
            hakemukset: valitut
        };
        laheta(batch);
    }

    async function laheta(paatokset: PaatosBatch) {
        try {
            const response = await Axios.post(paatoksetBatchUrl, paatokset);
            tyhjennaValinnatCallback();
        } catch (e) {
            // TODO: virheenkäsittely
            console.log(e);
        }
    }

    useEffect(() => {
        asetaKaytossa(valitut && valitut.length > 0);
    }, [valitut]);

    return (
        <div>
            <Button disabled={!kaytossa} onClick={_ => paatos(false)}>
                {i18n.translate('REKISTEROINNIT_HYLKAA_VALITUT')}
            </Button>
            <Button disabled={!kaytossa} onClick={_ => paatos(true)}>
                {i18n.translate('REKISTEROINNIT_HYVAKSY_VALITUT')}
            </Button>
        </div>
    );
}
