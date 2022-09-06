import React, { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import axios from 'axios';
import { setLocale } from 'yup';

import store from './store';
import { JotpaOrganisaatio } from './JotpaOrganisaatio';
import { fetchOrganization, OrganizationSchema } from '../organizationSlice';
import { KoodistoContext, Koodistos } from '../KoodistoContext';
import { Koodi, Language } from '../types';
import { JotpaPaakayttaja } from './JotpaPaakayttaja';
import { JotpaWizardValidator } from './JotpaWizardValidator';
import { useLanguageContext } from '../LanguageContext';
import { UserSchema } from '../userSlice';
import { JotpaYhteenveto } from './JotpaYhteenveto';

store.dispatch(fetchOrganization());
setLocale({
    mixed: {
        required: 'validaatio_pakollinen',
    },
    string: {
        matches: 'validaatio_geneerinen',
        email: 'validaatio_email',
    },
});

const koodistoNimiComparator = (language: Language) => (a: Koodi, b: Koodi) =>
    (a.nimi[language] ?? 'xxx') > (b.nimi[language] ?? 'xxx') ? 1 : -1;

export function JotpaRekisterointi() {
    const { language } = useLanguageContext();
    const [koodisto, setKoodisto] = useState<Koodistos>();
    useEffect(() => {
        async function fetchKoodisto() {
            const [
                { data: kunnat },
                { data: yritysmuodot },
                { data: organisaatiotyypit },
                { data: maat },
                { data: posti },
            ] = await Promise.all([
                axios.get<Koodi[]>('/api/koodisto/KUNTA/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/YRITYSMUOTO/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/ORGANISAATIOTYYPPI/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/MAAT_JA_VALTIOT_1/koodi?onlyValid=true'),
                axios.get<Koodi[]>('/api/koodisto/POSTI/koodi?onlyValid=true'),
            ]);
            kunnat.sort(koodistoNimiComparator(language));
            yritysmuodot.sort(koodistoNimiComparator(language));
            organisaatiotyypit.sort(koodistoNimiComparator(language));
            maat.sort(koodistoNimiComparator(language));
            const postinumerot = posti.map((p) => p.arvo);
            setKoodisto({
                kunnat,
                yritysmuodot,
                organisaatiotyypit,
                maat,
                posti,
                postinumerot,
            });
        }

        void fetchKoodisto();
    }, []);

    if (!koodisto) {
        return <div></div>;
    }

    const organizationValidation = {
        slice: 'organization' as const,
        schema: OrganizationSchema(koodisto.yritysmuodot, koodisto.kunnat, koodisto.postinumerot, language),
        redirectPath: '/hakija/jotpa/organisaatio',
    };
    const userValidation = {
        slice: 'user' as const,
        schema: UserSchema,
        redirectPath: '/hakija/jotpa/paakayttaja',
    };

    return (
        <KoodistoContext.Provider value={koodisto}>
            <Provider store={store}>
                <Routes>
                    <Route path="/organisaatio" element={<JotpaOrganisaatio />} />
                    <Route
                        path="/paakayttaja"
                        element={
                            <JotpaWizardValidator validate={[organizationValidation]}>
                                <JotpaPaakayttaja />
                            </JotpaWizardValidator>
                        }
                    />
                    <Route
                        path="/yhteenveto"
                        element={
                            <JotpaWizardValidator validate={[organizationValidation, userValidation]}>
                                <JotpaYhteenveto />
                            </JotpaWizardValidator>
                        }
                    />
                </Routes>
            </Provider>
        </KoodistoContext.Provider>
    );
}
