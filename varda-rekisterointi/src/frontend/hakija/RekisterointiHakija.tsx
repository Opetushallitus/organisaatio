import React, { useEffect, useState, useReducer, useContext } from 'react';
import { cloneDeep } from 'lodash';
import { KoodiUri, Organisaatio, tyhjaOrganisaatio } from '../types/types';
import Spinner from '../Spinner';
import Axios from 'axios';
import Rekisterointi from './Rekisterointi';
import ErrorPage from '../virhe/VirheSivu';
import { LanguageContext } from '../contexts';

const organisaatiotUrl = '/varda-rekisterointi/hakija/api/organisaatiot';
const rekisteroinnitUrl = '/varda-rekisterointi/hakija/api/rekisteroinnit';

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function RekisterointiHakija() {
    const { i18n } = useContext(LanguageContext);
    const [initialOrganisaatio, setInitialOrganisaatio] = useState(tyhjaOrganisaatio());
    const [organisaatio, setOrganisaatio] = useReducer(reducer, tyhjaOrganisaatio());
    const [fetchLoading, setFetchLoading] = useState(true);
    const [fetchError, setFetchError] = useState<unknown>(null);
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
                setInitialOrganisaatio({ ...tyhjaOrganisaatio(), ...cloneDeep(data), tyypit: tyypit });
                setOrganisaatio({ ...tyhjaOrganisaatio(), ...cloneDeep(data), tyypit: tyypit });
            } catch (error: unknown) {
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
        return <ErrorPage>{i18n.translate('ERROR_FETCH')}</ErrorPage>;
    }

    return (
        <Rekisterointi
            initialOrganisaatio={initialOrganisaatio}
            organisaatio={organisaatio as Organisaatio}
            setOrganisaatio={setOrganisaatio}
            rekisteroinnitUrl={rekisteroinnitUrl}
        />
    );
}
