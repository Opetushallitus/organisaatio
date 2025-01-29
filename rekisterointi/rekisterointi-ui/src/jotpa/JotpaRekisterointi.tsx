import React, { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import axios from 'axios';
import { setLocale } from 'yup';
import { Helmet } from 'react-helmet';

import store from './store';
import { JotpaOrganisaatio } from './JotpaOrganisaatio';
import { fetchOrganisation, OrganisationSchema } from '../organisationSlice';
import { KoodistoContext, Koodistos } from '../KoodistoContext';
import { Koodi, Language } from '../types';
import { JotpaPaakayttaja } from './JotpaPaakayttaja';
import { JotpaWizardValidator } from './JotpaWizardValidator';
import { useLanguageContext } from '../LanguageContext';
import { UserSchema } from '../userSlice';
import { JotpaYhteenveto } from './JotpaYhteenveto';

setLocale({
    mixed: {
        required: 'validaatio_pakollinen',
        notType: 'validaatio_geneerinen',
    },
    string: {
        matches: 'validaatio_geneerinen',
        email: 'validaatio_email',
    },
});

const koodistoNimiComparator = (language: Language) => (a: Koodi, b: Koodi) =>
    (a.nimi[language] ?? 'xxx') > (b.nimi[language] ?? 'xxx') ? 1 : -1;

export function JotpaRekisterointi() {
    store.dispatch(fetchOrganisation());
    const { language, i18n } = useLanguageContext();
    const [koodisto, setKoodisto] = useState<Koodistos>({
        kunnat: [],
        yritysmuodot: [],
        organisaatiotyypit: [],
        posti: [],
        postinumerot: [],
    });
    useEffect(() => {
        async function fetchKoodisto() {
            const [{ data: kunnat }, { data: yritysmuodot }, { data: organisaatiotyypit }, { data: posti }] =
                await Promise.all([
                    axios.get<Koodi[]>('/api/koodisto/KUNTA/koodi?onlyValid=true'),
                    axios.get<Koodi[]>('/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true'),
                    axios.get<Koodi[]>('/api/koodisto/ORGANISAATIOTYYPPI/koodi?onlyValid=true'),
                    axios.get<Koodi[]>('/api/koodisto/POSTI/koodi?onlyValid=true'),
                ]);
            kunnat.sort(koodistoNimiComparator(language));
            yritysmuodot.sort(koodistoNimiComparator(language));
            organisaatiotyypit.sort(koodistoNimiComparator(language));
            const postinumerot = posti.map((p) => p.arvo);
            setKoodisto({
                kunnat,
                yritysmuodot,
                organisaatiotyypit,
                posti,
                postinumerot,
            });
        }

        void fetchKoodisto();
    }, []);

    const organisationValidation = {
        slice: 'organisation' as const,
        schema: OrganisationSchema(koodisto.yritysmuodot, koodisto.kunnat, koodisto.postinumerot),
        redirectPath: '/hakija/jotpa/organisaatio',
    };
    const userValidation = {
        slice: 'user' as const,
        schema: UserSchema,
        redirectPath: '/hakija/jotpa/paakayttaja',
    };

    return (
        <KoodistoContext.Provider value={koodisto}>
            <Helmet>
                <title>{i18n.translate('title')}</title>
            </Helmet>
            <Provider store={store}>
                <Routes>
                    <Route path="/organisaatio" element={<JotpaOrganisaatio />} />
                    <Route
                        path="/paakayttaja"
                        element={
                            <JotpaWizardValidator validate={[organisationValidation]}>
                                <JotpaPaakayttaja />
                            </JotpaWizardValidator>
                        }
                    />
                    <Route
                        path="/yhteenveto"
                        element={
                            <JotpaWizardValidator validate={[organisationValidation, userValidation]}>
                                <JotpaYhteenveto />
                            </JotpaWizardValidator>
                        }
                    />
                </Routes>
            </Provider>
        </KoodistoContext.Provider>
    );
}
