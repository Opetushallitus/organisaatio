import React, {useContext, useEffect, useState} from "react";
import Axios from "axios";
import {LanguageContext} from '../contexts';
import {Rekisterointihakemus, Tila} from "./rekisterointihakemus";

import Box from "@opetushallitus/virkailija-ui-components/Box";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

import styles from "./RekisterointiLista.module.css";
import RekisterointiListaOtsikko from "./RekisterointiListaOtsikko";
import RekisterointiListaRivi, {ListaRivi} from "./RekisterointiListaRivi";

const rekisteroinnitUrl = "/varda-rekisterointi/virkailija/api/rekisteroinnit";
const tyhjaLista: Rekisterointihakemus[] = [];

type Props = {
    tila?: Tila,
    hakutermi?: string
}

export default function RekisterointiLista({ tila = Tila.KASITTELYSSA, hakutermi } : Props) {
    const { i18n } = useContext(LanguageContext);
    const [rekisteroinnit, asetaRekisteroinnit] = useState(tyhjaLista);
    const [latausKesken, asetaLatausKesken] = useState(true);
    const [latausVirhe, asetaLatausVirhe] = useState(false);
    const [kaikkiValittu, asetaKaikkiValittu] = useState(false);

    useEffect(() => {
        async function lataa() {
            try {
                asetaLatausKesken(true);
                asetaLatausVirhe(false);
                const params = { tila, hakutermi};
                const response = await Axios.get(rekisteroinnitUrl, { params });
                asetaRekisteroinnit(response.data);
            } catch (e) {
                asetaLatausVirhe(true);
                console.error(e);
            } finally {
                asetaLatausKesken(false);
            }
        }
        lataa();
    }, [tila, hakutermi]);

    function vaihdaKaikkiValittu() {
        asetaKaikkiValittu(!kaikkiValittu);
    }

    if (latausKesken) {
        return <Spin />;
    }

    if (latausVirhe) {
        return <div className="virhe">{i18n.translate('REKISTEROINNIT_LATAUSVIRHE')}</div>
    }

    return (
        <Box>
            <table className={styles.vardaRekisterointiLista}>
            <RekisterointiListaOtsikko
                kaikkiValittu={kaikkiValittu}
                kaikkiValittuCallback={vaihdaKaikkiValittu}/>
                <tbody>
            {
                rekisteroinnit.map(rekisterointi =>
                    <RekisterointiListaRivi key={rekisterointi.id} rekisterointi={new ListaRivi(rekisterointi)} kaikkiValittu={kaikkiValittu}/>)
            }
                </tbody>
            </table>
        </Box>
    )
}
