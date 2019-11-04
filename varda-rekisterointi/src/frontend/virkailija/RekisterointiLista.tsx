import React, {useContext, useEffect, useState} from "react";
import Axios from "axios";
import {LanguageContext} from '../contexts';
import {Rekisterointihakemus, Tila} from "./rekisterointihakemus";

import Box from "@opetushallitus/virkailija-ui-components/Box";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

import styles from "./RekisterointiLista.module.css";
import RekisterointiListaOtsikko from "./RekisterointiListaOtsikko";
import RekisterointiListaRivi, {ListaRivi} from "./RekisterointiListaRivi";
import PaatosKontrollit from "./PaatosKontrollit";

const rekisteroinnitUrl = "/varda-rekisterointi/virkailija/api/rekisteroinnit";
const tyhjaHakemusLista: Rekisterointihakemus[] = [];
const tyhjaValintaLista: number[] = [];

type Props = {
    tila?: Tila,
    hakutermi?: string
}

export default function RekisterointiLista({ tila = Tila.KASITTELYSSA, hakutermi } : Props) {
    const { i18n } = useContext(LanguageContext);
    const [rekisteroinnit, asetaRekisteroinnit] = useState(tyhjaHakemusLista);
    const [latausKesken, asetaLatausKesken] = useState(true);
    const [latausVirhe, asetaLatausVirhe] = useState(false);
    const [kaikkiValittu, asetaKaikkiValittu] = useState(false);
    const [valitutHakemukset, asetaValitutHakemukset] = useState(tyhjaValintaLista);

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
                console.error(e); // TODO: vain dev modessa?
            } finally {
                asetaLatausKesken(false);
            }
        }
        lataa();
    }, [tila, hakutermi]);

    function vaihdaKaikkiValittu(valitseKaikki: boolean) {
        asetaKaikkiValittu(valitseKaikki);
        asetaValitutHakemukset(valitseKaikki ? rekisteroinnit.map(r => r.id) : tyhjaValintaLista);
    }

    function vaihdaHakemusValittu(id: number, valittu: boolean) {
        asetaKaikkiValittu(false);
        asetaValitutHakemukset(valittu ? valitutHakemukset.filter(h => h !== id) : valitutHakemukset.concat(id));
    }

    function tyhjennaValinnat() {
        vaihdaKaikkiValittu(false);
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
                    <RekisterointiListaRivi key={rekisterointi.id}
                                            rekisterointi={new ListaRivi(rekisterointi)}
                                            riviValittu={valitutHakemukset.some(h => h === rekisterointi.id)}
                                            valitseHakemusCallback={vaihdaHakemusValittu}/>)
            }
                </tbody>
            </table>
            {
                tila === Tila.KASITTELYSSA &&
                <PaatosKontrollit valitut={valitutHakemukset} tyhjennaValinnatCallback={tyhjennaValinnat} />
            }
        </Box>
    )
}
