import React from "react";
import {render, unmountComponentAtNode} from "react-dom";
import {act} from "react-dom/test-utils";
import RekisterointiLista from "./RekisterointiLista";
import {Rekisterointihakemus, Tila} from "./rekisterointihakemus";
import {Kayttaja, Organisaatio} from "../types";

const dummyKayttaja: Kayttaja = {
    asiointikieli: "fi",
    etunimi: "Testi",
    sukunimi: "Henkilö",
    sahkoposti: "testi.henkilo@foo.bar",
    saateteksti: "foo"
};
const dummyOrganisaatio: Organisaatio = {
    nimi: {
        "fi": "Oy Firma Ab"
    },
    ytunnus: "12345678-9",
    alkuPvm: "1999-01-01",
    kieletUris: [],
    kotipaikkaUri: "",
    maaUri: "",
    nimet: [],
    oid: "12345",
    tyypit: [],
    yhteystiedot: [],
    yritysmuoto: ""
};

let container: Element;
describe('RekisterointiLista', () => {
    beforeEach(() => {
        container = document.createElement('div');
        document.body.appendChild(container);
    });
    afterEach(() => {
        unmountComponentAtNode(container);
        container.remove();
    });

    it('tulostuu tyhjänä', () => {
        const rekisteroinnit: Rekisterointihakemus[] = [];
        act(() => {
            render(<RekisterointiLista rekisteroinnit={rekisteroinnit}/>, container);
        });
        expect(container.querySelector("table.varda-lista")).not.toBeNull();
    });

    it('tulostaa rivejä', () => {
        const rekisterointi: Rekisterointihakemus = {
            kayttaja: dummyKayttaja,
            organisaatio: dummyOrganisaatio,
            toimintamuoto: "",
            sahkopostit: [],
            saapumisaika: new Date(),
            id: 1,
            tila: Tila.KASITTELYSSA
        };
        const rekisteroinnit = [rekisterointi];
        act(() => {
            render(<RekisterointiLista rekisteroinnit={rekisteroinnit}/>, container);
        });
        expect(container.querySelectorAll("tr.varda-lista-rivi")).toHaveLength(rekisteroinnit.length);
    });
});
