import React, { useEffect, useState, useReducer } from 'react';
import { Organisaatio, KoodiUri } from '../types';
import Spinner from '../Spinner';
import Axios from 'axios';
import Rekisterointi from './Rekisterointi';

const baseOrganisaatio: Organisaatio = {
    ytunnus: '',
    ytjNimi: {
        nimi: '',
        alkuPvm: null,
        kieli: 'fi'
    },
    alkuPvm: null,
    yritysmuoto: '',
    tyypit: [],
    kotipaikkaUri: '',
    maaUri: 'maatjavaltiot1_fin',
    yhteystiedot: []
};

const organisaatiotUrl = "/varda-rekisterointi/hakija/api/organisaatiot";
const rekisteroinnitUrl = "/varda-rekisterointi/hakija/api/rekisteroinnit";

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function RekisterointiHakija() {
    const [initialOrganisaatio, setInitialOrganisaatio] = useState(baseOrganisaatio);
    const [organisaatio, setOrganisaatio] = useReducer(reducer, baseOrganisaatio);
    const [fetchLoading, setFetchLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);
    useEffect(() => {
        async function fetch() {
            try {
                setFetchLoading(true);
                setFetchError(null);
                const response = await Axios.get(organisaatiotUrl);
                const data = response.data;
                const tyypit: KoodiUri[] = data.tyypit ? data.tyypit : [];
                if (tyypit.indexOf('organisaatiotyyppi_07') === -1) {
                    tyypit.push('organisaatiotyyppi_07');
                }
                setInitialOrganisaatio({ ...baseOrganisaatio, ...data, tyypit: tyypit });
                setOrganisaatio({ ...baseOrganisaatio, ...data, tyypit: tyypit });
            } catch (error) {
                setFetchError(error);
            } finally {
                setFetchLoading(false);
            }
        }
        fetch();
    }, []);

    if (fetchLoading) {
        return <Spinner />;
    }
    if (fetchError) {
        return <div>error, reload page</div>;
    }

    return <Rekisterointi initialOrganisaatio={initialOrganisaatio}
                          organisaatio={organisaatio}
                          setOrganisaatio={setOrganisaatio}
                          rekisteroinnitUrl={rekisteroinnitUrl} />;
}
