import React, { useState, useReducer } from 'react';
import Rekisterointi from '../hakija/Rekisterointi';
import { Organisaatio } from '../types';

const baseOrganisaatio: Organisaatio = {
    ytunnus: '',
    ytjNimi: {
        nimi: '',
        alkuPvm: null,
        kieli: 'fi'
    },
    alkuPvm: null,
    yritysmuoto: '',
    tyypit: ['organisaatiotyyppi_07'],
    kotipaikkaUri: '',
    maaUri: 'maatjavaltiot1_fin',
    yhteystiedot: []
};

const rekisteroinnitUrl = "/varda-rekisterointi/virkailija/api/rekisteroinnit";

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function RekisterointiVirkailija() {
    const [initialOrganisaatio] = useState(baseOrganisaatio);
    const [organisaatio, setOrganisaatio] = useReducer(reducer, baseOrganisaatio);

    return <Rekisterointi initialOrganisaatio={initialOrganisaatio}
                          organisaatio={organisaatio}
                          setOrganisaatio={setOrganisaatio}
                          rekisteroinnitUrl={rekisteroinnitUrl} />
}
