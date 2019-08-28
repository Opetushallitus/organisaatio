import React, { useState, useReducer, useEffect } from 'react';
import RekisterointiOrganisaatio from './RekisterointiOrganisaatio';
import RekisterointiKayttaja from './RekisterointiKayttaja';
import { Organisaatio, Kayttaja, KoodiUri } from '../types';
import RekisterointiYhteenveto from './RekisterointiYhteenveto';
import RekisterointiValmis from './RekisterointiValmis';
import Axios from 'axios';
import './index.css';
import Header from './Header';
import Wizard from '../Wizard';
import Navigation from './Navigation';
import Spinner from '../Spinner';

const baseOrganisaatio: Organisaatio = {
    ytunnus: '',
    nimi: {
        fi: '',
        sv: '',
        en: '',
    },
    alkuPvm: '2019-01-01',
    nimet: [{
        alkuPvm: '2019-01-01',
        nimi: {
            fi: '',
            sv: '',
            en: '',
        },
    }],
    yritysmuoto: '',
    kieletUris: [],
    tyypit: [],
    kotipaikkaUri: '',
    maaUri: 'maatjavaltiot1_fin',
    yhteystiedot: [],
};
const intialSahkopostit = [''];
const initialToimintamuoto = 'vardatoimintamuoto_tm01';
const initialKayttaja: Kayttaja = {
    etunimi: '',
    sukunimi: '',
    sahkoposti: '',
    asiointikieli: 'fi',
    saateteksti: '',
}

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function Rekisterointi() {
    const [initialOrganisaatio, setInitialOrganisaatio] = useState(baseOrganisaatio);
    const [organisaatio, setOrganisaatio] = useReducer(reducer, baseOrganisaatio);
    const [sahkopostit, setSahkopostit] = useState(intialSahkopostit);
    const [toimintamuoto, setToimintamuoto] = useState(initialToimintamuoto);
    const [kayttaja, setKayttaja] = useReducer(reducer, initialKayttaja);
    const [fetchLoading, setFetchLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);
    const [postLoading, setPostLoading] = useState(false);
    const [postError, setPostError] = useState(null);

    useEffect(() => {
        async function fetch() {
            try {
                setFetchLoading(true);
                setFetchError(null);
                const response = await Axios.get('/varda-rekisterointi/hakija/api/organisaatio');
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

    async function post() {
        try {
            setPostLoading(true);
            setPostError(null);
            await Axios.post('/varda-rekisterointi/hakija/api/rekisterointi', {
                organisaatio: organisaatio,
                sahkopostit: sahkopostit,
                toimintamuoto: toimintamuoto,
                kayttaja: kayttaja,
            });
        } catch (error) {
            setPostError(error);
            throw error;
        } finally {
            setPostLoading(false);
        }
    }

    if (fetchLoading) {
        return <Spinner />;
    }
    if (fetchError) {
        return <div>error, reload page</div>;
    }

    return (
        <div className="varda-rekisterointi-hakija">
            <Header />
            <Wizard getNavigation={currentStep => <Navigation currentStep={currentStep} />}
                    disabled={false}
                    changed={() => setPostError(null)}
                    submit={post}
                    loading={postLoading}
                    error={postError ? 'error, try again' : undefined}>
                <RekisterointiOrganisaatio
                    initialOrganisaatio={initialOrganisaatio}
                    organisaatio={organisaatio}
                    setOrganisaatio={setOrganisaatio}
                    sahkopostit={sahkopostit}
                    setSahkopostit={setSahkopostit} />
                <RekisterointiKayttaja
                    toimintamuoto={toimintamuoto}
                    setToimintamuoto={setToimintamuoto}
                    kayttaja={kayttaja}
                    setKayttaja={setKayttaja} />
                <RekisterointiYhteenveto
                    organisaatio={organisaatio}
                    sahkopostit={sahkopostit}
                    toimintamuoto={toimintamuoto}
                    kayttaja={kayttaja} />
                <RekisterointiValmis />
            </Wizard>
        </div>
    );
}
