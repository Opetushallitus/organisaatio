import React, { useState, useReducer, useContext } from 'react';
import RekisterointiOrganisaatio from './RekisterointiOrganisaatio';
import RekisterointiKayttaja from './RekisterointiKayttaja';
import { Organisaatio, Kayttaja } from '../types';
import RekisterointiYhteenveto from './RekisterointiYhteenveto';
import Axios from 'axios';
import './Rekisterointi.css';
import Header from './Header';
import Wizard from '../Wizard';
import Navigation from './Navigation';
import {KuntaKoodistoContext, LanguageContext} from '../contexts';
import EmailValidator from 'email-validator';
import * as YtunnusValidator from '../YtunnusValidator';
import { kielletytYritysmuodot } from './YritysmuotoUtils';
import {validoiYhteystiedot} from "../YhteystiedotValidator";

type Props = {
    initialOrganisaatio: Organisaatio,
    organisaatio: Organisaatio,
    setOrganisaatio: (organisaatio: Partial<Organisaatio>) => void,
    rekisteroinnitUrl: string,
}

const initialKunnat: string[] = [];
const intialSahkopostit: string[] = [];
const initialToimintamuoto = 'vardatoimintamuoto_tm01';
const initialKayttaja: Kayttaja = {
    etunimi: '',
    sukunimi: '',
    sahkoposti: '',
    asiointikieli: 'fi',
    saateteksti: '',
};

function reducer<T>(state: T, data: Partial<T>): T {
    return { ...state, ...data };
}

export default function Rekisterointi({initialOrganisaatio, organisaatio, setOrganisaatio, rekisteroinnitUrl}: Props) {
    const { i18n } = useContext(LanguageContext);
    const { koodisto: kuntaKoodisto } = useContext(KuntaKoodistoContext);
    const [organisaatioErrors, setOrganisaatioErrors] = useState({});
    const [kunnat, setKunnat] = useState(initialKunnat);
    const [sahkopostit, setSahkopostit] = useState(intialSahkopostit);
    const [toimintamuoto, setToimintamuoto] = useState(initialToimintamuoto);
    const [kayttaja, setKayttaja] = useReducer(reducer, initialKayttaja);
    const [kayttajaErrors, setKayttajaErrors] = useState({});
    const [postLoading, setPostLoading] = useState(false);
    const [postError, setPostError] = useState(null);

    async function post() {
        try {
            setPostLoading(true);
            setPostError(null);
            const response = await Axios.post(rekisteroinnitUrl, {
                organisaatio: organisaatio,
                kunnat: kunnat,
                sahkopostit: sahkopostit,
                toimintamuoto: toimintamuoto,
                kayttaja: kayttaja,
            });
            window.location = response.data;
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
                if (!organisaatio.oid) {
                    ['ytunnus', 'yritysmuoto', 'kotipaikkaUri', 'alkuPvm']
                        .filter(field => !(organisaatio as any)[field])
                        .forEach(field => organisaatioErrors[field] = i18n.translate('PAKOLLINEN_TIETO'));
                    if (organisaatio.ytunnus && !YtunnusValidator.validate(organisaatio.ytunnus)) {
                        organisaatioErrors.ytunnus = i18n.translate('VIRHEELLINEN_YTUNNUS');
                    }
                    const yhteystietoVirheet = validoiYhteystiedot(organisaatio.yhteystiedot);
                    for (let avain in yhteystietoVirheet) {
                        organisaatioErrors[`yhteystiedot.${avain}`] = i18n.translate(yhteystietoVirheet[avain]);
                    }
                }
                if (kielletytYritysmuodot.includes(organisaatio.yritysmuoto)) {
                    organisaatioErrors.yritysmuoto = i18n.translate('VIRHEELLINEN_YRITYSMUOTO');
                }
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
    const kaikkiKunnat = kuntaKoodisto.koodit();

    return (
        <div className="varda-rekisterointi-hakija">
            <Header />
            <Wizard getNavigation={currentStep => <Navigation currentStep={currentStep} />}
                    disabled={false}
                    validate={validate}
                    submit={post}
                    loading={postLoading}
                    error={postError ? i18n.translate('ERROR_SAVE') : undefined}>
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
            </Wizard>
        </div>
    );
}
