import React, { useState, useReducer, useEffect, useContext } from 'react';
import RekisterointiOrganisaatio from './RekisterointiOrganisaatio';
import RekisterointiKayttaja from './RekisterointiKayttaja';
import { Organisaatio, Kayttaja, KoodiUri } from '../types';
import RekisterointiYhteenveto from './RekisterointiYhteenveto';
import RekisterointiValmis from './RekisterointiValmis';
import Axios from 'axios';
import './Rekisterointi.css';
import Header from './Header';
import Wizard from '../Wizard';
import Navigation from './Navigation';
import Spinner from '../Spinner';
import { LanguageContext } from '../contexts';
import EmailValidator from 'email-validator';
import { useBeforeunload } from 'react-beforeunload';

const baseOrganisaatio: Organisaatio = {
    ytunnus: '',
    nimi: {
        fi: '',
        sv: '',
        en: '',
    },
    alkuPvm: null,
    nimet: [{
        alkuPvm: null,
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
    ytjkieli: 'kieli_fi#1',
};
const intialSahkopostit: string[] = [];
const initialToimintamuoto = 'vardatoimintamuoto_tm01';
const initialKayttaja: Kayttaja = {
    etunimi: '',
    sukunimi: '',
    sahkoposti: '',
    asiointikieli: 'fi',
    saateteksti: '',
}
const organisaatiotUrl = "/varda-rekisterointi/hakija/api/organisaatiot";
const rekisteroinnitUrl = "/varda-rekisterointi/hakija/api/rekisteroinnit";

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function Rekisterointi() {
    const { i18n } = useContext(LanguageContext);
    const [initialOrganisaatio, setInitialOrganisaatio] = useState(baseOrganisaatio);
    const [organisaatio, setOrganisaatio] = useReducer(reducer, baseOrganisaatio);
    const [organisaatioErrors, setOrganisaatioErrors] = useState({});
    const [sahkopostit, setSahkopostit] = useState(intialSahkopostit);
    const [toimintamuoto, setToimintamuoto] = useState(initialToimintamuoto);
    const [kayttaja, setKayttaja] = useReducer(reducer, initialKayttaja);
    const [kayttajaErrors, setKayttajaErrors] = useState({});
    const [fetchLoading, setFetchLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);
    const [postLoading, setPostLoading] = useState(false);
    const [postError, setPostError] = useState(null);
    const [ready, setReady] = useState(false);
    useBeforeunload(event => {
        if (!ready) {
            event.preventDefault();
        }
    });

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

    async function post() {
        try {
            setPostLoading(true);
            setPostError(null);
            await Axios.post(rekisteroinnitUrl, {
                organisaatio: organisaatio,
                sahkopostit: sahkopostit,
                toimintamuoto: toimintamuoto,
                kayttaja: kayttaja,
            });
            setReady(true);
        } catch (error) {
            setPostError(error);
            throw error;
        } finally {
            setPostLoading(false);
        }
    }

    function validate(currentStep: number): boolean {
        setPostError(null);
        switch (currentStep) {
            case 1:
                const organisaatioErrors: Record<string, string> = {};
                ['ytunnus', 'yritysmuoto', 'kotipaikkaUri', 'alkuPvm']
                    .filter(field => !organisaatio[field])
                    .forEach(field => organisaatioErrors[field] = i18n.translate('PAKOLLINEN_TIETO'));
                if (sahkopostit.some(sahkoposti => !EmailValidator.validate(sahkoposti))) {
                    organisaatioErrors.sahkopostit = i18n.translate('VIRHEELLINEN_SAHKOPOSTI');
                }
                setOrganisaatioErrors(organisaatioErrors);
                return Object.keys(organisaatioErrors).length === 0;
            case 2:
                const kayttajaErrors: Record<string, string> = {};
                ['etunimi', 'sukunimi', 'sahkoposti', 'asiointikieli']
                    .filter(field => !kayttaja[field])
                    .forEach(field => kayttajaErrors[field] = i18n.translate('PAKOLLINEN_TIETO'));
                if (!!kayttaja.sahkoposti && !EmailValidator.validate(kayttaja.sahkoposti)) {
                    kayttajaErrors.sahkoposti = i18n.translate('VIRHEELLINEN_SAHKOPOSTI');
                }
                setKayttajaErrors(kayttajaErrors);
                return Object.keys(kayttajaErrors).length === 0;
        }
        return true;
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
                    validate={validate}
                    submit={post}
                    loading={postLoading}
                    error={postError ? 'error, try again' : undefined}>
                <RekisterointiOrganisaatio
                    initialOrganisaatio={initialOrganisaatio}
                    organisaatio={organisaatio}
                    setOrganisaatio={setOrganisaatio}
                    sahkopostit={sahkopostit}
                    setSahkopostit={setSahkopostit}
                    errors={organisaatioErrors} />
                <RekisterointiKayttaja
                    toimintamuoto={toimintamuoto}
                    setToimintamuoto={setToimintamuoto}
                    kayttaja={kayttaja}
                    setKayttaja={setKayttaja}
                    errors={kayttajaErrors} />
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
