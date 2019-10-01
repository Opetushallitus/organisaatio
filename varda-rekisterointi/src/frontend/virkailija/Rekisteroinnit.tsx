import React, {useContext, useEffect, useState} from "react";
import {LanguageContext} from '../contexts';
import RekisterointiLista from './RekisterointiLista';
import Axios from "axios";
import {Rekisterointihakemus} from "./rekisterointihakemus";
import Spinner from "../Spinner";

const rekisteroinnitUrl = "/varda-rekisterointi/virkailija/api/rekisteroinnit";
const tyhjaLista: Rekisterointihakemus[] = [];

export default function Rekisteroinnit() {
    const { i18n } = useContext(LanguageContext);
    const [rekisteroinnit, asetaRekisteroinnit] = useState(tyhjaLista);
    const [latausKesken, asetaLatausKesken] = useState(true);
    const [latausVirhe, asetaLatausVirhe] = useState(false);

    useEffect(() => {
        async function lataa() {
            try {
                asetaLatausKesken(true);
                asetaLatausVirhe(false);
                const response = await Axios.get(rekisteroinnitUrl);
                const data = response.data;
                asetaRekisteroinnit(data);
            } catch (e) {
                asetaLatausVirhe(true);
                console.error(e);
            } finally {
                asetaLatausKesken(false);
            }
        }
        lataa();
    }, []);

    if (latausKesken) {
        return <Spinner/>;
    }

    if (latausVirhe) {
        return <div className="virhe">{i18n.translate('REKISTEROINNIT_LATAUSVIRHE')}</div>
    }

    return (
        <div className="varda-rekisteroinnit">
            <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
            <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
            <RekisterointiLista rekisteroinnit={rekisteroinnit}/>
        </div>
    );
}
