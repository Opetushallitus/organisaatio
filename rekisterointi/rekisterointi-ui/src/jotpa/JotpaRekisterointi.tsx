import React, { useEffect, useMemo } from 'react';
import { Routes, Route } from 'react-router';
import { setLocale } from 'yup';

import { useJotpaRekisterointiDispatch } from './store';
import { JotpaOrganisaatio } from './JotpaOrganisaatio';
import { OrganisationSchema, setInitialOrganisation } from '../organisationSlice';
import { KoodistoContext, Koodistos } from '../KoodistoContext';
import { Koodi, Language } from '../types';
import { JotpaPaakayttaja } from './JotpaPaakayttaja';
import { JotpaWizardValidator } from './JotpaWizardValidator';
import { useLanguageContext } from '../LanguageContext';
import { UserSchema } from '../userSlice';
import { JotpaYhteenveto } from './JotpaYhteenveto';
import { useGetKoodistoQuery, useGetOrganisationQuery } from '../rekisterointiApi';
import { usePageTitle } from '../documentHead';

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
    const dispatch = useJotpaRekisterointiDispatch();
    const { language, i18n } = useLanguageContext();
    usePageTitle(i18n.translate('title'));

    const { data: organisation, isError: organisationError } = useGetOrganisationQuery();
    const { data: kunnat = [] } = useGetKoodistoQuery('KUNTA');
    const { data: yritysmuodot = [] } = useGetKoodistoQuery('YRITYSMUOTO');
    const { data: organisaatiotyypit = [] } = useGetKoodistoQuery('ORGANISAATIOTYYPPI');
    const { data: posti = [] } = useGetKoodistoQuery('POSTI');

    useEffect(() => {
        if (organisation) {
            dispatch(setInitialOrganisation(organisation));
        }
    }, [dispatch, organisation]);

    useEffect(() => {
        if (organisationError) {
            window.location.href = '/hakija/logout?redirect=/jotpa';
        }
    }, [organisationError]);

    const koodisto: Koodistos = useMemo(() => {
        const sortKoodit = (koodit: Koodi[]) => [...koodit].sort(koodistoNimiComparator(language));
        return {
            kunnat: sortKoodit(kunnat),
            yritysmuodot: sortKoodit(yritysmuodot),
            organisaatiotyypit: sortKoodit(organisaatiotyypit),
            posti,
            postinumerot: posti.map((p) => p.arvo),
        };
    }, [kunnat, language, organisaatiotyypit, posti, yritysmuodot]);

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
        </KoodistoContext.Provider>
    );
}
