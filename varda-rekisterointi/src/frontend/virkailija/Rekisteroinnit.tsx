import React, {useContext} from "react";
import {LanguageContext} from '../contexts';
import RekisterointiLista from './RekisterointiLista';
import {Rekisterointihakemus, Tila} from "./rekisterointihakemus";

export default function Rekisteroinnit() {
    const { i18n } = useContext(LanguageContext);
    const rekisteroinnit: Rekisterointihakemus[] = [
        {
            kayttaja: {
                sukunimi: "Henkilö", etunimi: "Testi", saateteksti: "", sahkoposti: "foo@foo.bar", asiointikieli: "fi"
            },
            toimintamuoto: "",
            tila: Tila.KASITTELYSSA,
            id: 1,
            saapumisaika: new Date(),
            sahkopostit: ["foo@foo.bar"],
            organisaatio: {
                yritysmuoto: "", yhteystiedot: [], tyypit: [], oid: "123.4567.890", nimet: [], maaUri: "",
                kotipaikkaUri: "", kieletUris: [], alkuPvm: "", ytunnus: "1234567-8", nimi: { fi: "Oy Testi Ab" },
                ytjkieli: ""
            }
        }
    ];
    return (
        <div className="varda-rekisteroinnit">
            <h2>{i18n.translate('REKISTEROINNIT_OTSIKKO')}</h2>
            <p>{i18n.translate('REKISTEROINNIT_KUVAUS')}</p>
            <RekisterointiLista rekisteroinnit={rekisteroinnit}/>
        </div>
    );
}
