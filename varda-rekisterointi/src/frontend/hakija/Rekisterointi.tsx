import React, { useState, useReducer, useEffect, useContext } from 'react';
import RekisterointiOrganisaatio from './RekisterointiOrganisaatio';
import RekisterointiKayttaja from './RekisterointiKayttaja';
import { Organisaatio, Kayttaja, KoodiUri, Koodi } from '../types';
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
import useAxios from 'axios-hooks';
import { getYhteystietoArvo, isPuhelinnumero, toPuhelinnumero, isSahkoposti, toSahkoposti, isKayntiosoite, toOsoite, isPostiosoite, toPostinumeroUri } from '../OrganisaatioYhteystietoUtils';

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
const initialKunnat: string[] = [];
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
    const [{data: kaikkiKunnat, loading: kaikkiKunnatLoading, error: kaikkiKunnatError}]
        = useAxios<Koodi[]>('/varda-rekisterointi/api/koodisto/KUNTA/koodi?onlyValid=true');
    const [initialOrganisaatio, setInitialOrganisaatio] = useState(baseOrganisaatio);
    const [organisaatio, setOrganisaatio] = useReducer(reducer, baseOrganisaatio);
    const [organisaatioErrors, setOrganisaatioErrors] = useState({});
    const [kunnat, setKunnat] = useState(initialKunnat);
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
                kunnat: kunnat,
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
                [
                    { name: 'puhelinnumero', filter: isPuhelinnumero, mapper: toPuhelinnumero },
                    { name: 'sahkoposti', filter: isSahkoposti, mapper: toSahkoposti },
                    { name: 'postiosoite', filter: isPostiosoite, mapper: toOsoite },
                    { name: 'postinumero', filter: isPostiosoite, mapper: toPostinumeroUri },
                    { name: 'kayntiosoite', filter: isKayntiosoite, mapper: toOsoite, },
                    { name: 'kayntiosoitteenPostinumero', filter: isKayntiosoite, mapper: toPostinumeroUri, },
                ].filter(field => !getYhteystietoArvo(organisaatio.yhteystiedot, field.filter, field.mapper))
                 .forEach(field => organisaatioErrors[field.name] = i18n.translate('PAKOLLINEN_TIETO'));
                if (kunnat.length === 0) {
                    organisaatioErrors.kunnat = i18n.translate('PAKOLLINEN_TIETO');
                }
                if (sahkopostit.length === 0) {
                    organisaatioErrors.sahkopostit = i18n.translate('PAKOLLINEN_TIETO');
                }
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

    if (fetchLoading || kaikkiKunnatLoading) {
        return <Spinner />;
    }
    if (fetchError || kaikkiKunnatError) {
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
                    kaikkiKunnat={kaikkiKunnat}
                    kunnat={kunnat}
                    setKunnat={setKunnat}
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
                    kaikkiKunnat={kaikkiKunnat}
                    kunnat={kunnat}
                    sahkopostit={sahkopostit}
                    toimintamuoto={toimintamuoto}
                    kayttaja={kayttaja} />
                <RekisterointiValmis />
            </Wizard>
        </div>
    );
}
