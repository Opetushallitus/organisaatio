import React, { useState, useReducer, useEffect, useContext } from 'react';
import Rekisterointi from '../hakija/Rekisterointi';
import { Organisaatio, KoodiUri } from '../types';
import { useParams } from 'react-router';
import Axios from 'axios';
import Spinner from '../Spinner';
import ErrorPage from '../ErrorPage';
import { LanguageContext } from '../contexts';

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
const organisaatiotUrl = (ytunnus: string | undefined): string => `/varda-rekisterointi/virkailija/api/organisaatiot/ytunnus=${ytunnus}`;

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function RekisterointiVirkailija() {
    const { i18n } = useContext(LanguageContext);
    const [initialOrganisaatio, setInitialOrganisaatio] = useState(baseOrganisaatio);
    const [organisaatio, setOrganisaatio] = useReducer(reducer, baseOrganisaatio);
    const [fetchLoading, setFetchLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);
    const { ytunnus } = useParams();
    useEffect(() => {
        async function fetch() {
            try {
                setFetchLoading(true);
                setFetchError(null);
                const response = await Axios.get(organisaatiotUrl(ytunnus));
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
    }, [ytunnus]);

    if (fetchLoading) {
        return <Spinner />;
    }
    if (fetchError) {
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    return <Rekisterointi initialOrganisaatio={initialOrganisaatio}
                          organisaatio={organisaatio}
                          setOrganisaatio={setOrganisaatio}
                          rekisteroinnitUrl={rekisteroinnitUrl} />
}
