import React, {useContext} from "react";
import { LanguageContext } from '../contexts';
import RekisterointiLista from './RekisterointiLista';
import {Rekisterointihakemus} from "./rekisterointihakemus";

export default function Rekisteroinnit() {
    const { i18n } = useContext(LanguageContext);
    const rekisteroinnit: Rekisterointihakemus[] = [];
    return (
        <div className="varda-rekisteroinnit">
            <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
            <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
            <RekisterointiLista rekisteroinnit={rekisteroinnit}/>
        </div>
    );
}
