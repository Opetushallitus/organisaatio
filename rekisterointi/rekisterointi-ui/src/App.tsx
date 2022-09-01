import axios from 'axios';
import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Outlet, Route, Routes } from 'react-router-dom';
import { registerLocale } from 'react-datepicker';
import fi from 'date-fns/locale/fi';
import { setLocale } from 'yup';

// import global styles first
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css';
import 'react-datepicker/dist/react-datepicker.css';
import './styles.css';

import { defaultLokalisointi, I18nImpl, LanguageContext } from './contexts';
import { Language, Lokalisointi, LokalisointiRivi } from './types';
import { JotpaRekisterointi } from './jotpa/JotpaRekisterointi';
import { JotpaLanding } from './jotpa/JotpaLanding';
import { Footer } from './Footer';

registerLocale('fi', fi);
setLocale({
    mixed: {
        required: 'Pakollinen tieto',
        notType: 'Virheellinen arvo',
    },
    string: {
        min: ({ min }) => `Kentän minimipituus on ${min} merkkiä`,
        max: ({ max }) => `Kentän maksimipituus on ${max} merkkiä`,
        matches: 'Virheellinen arvo',
        email: 'Sähköpostin muoto on väärä',
    },
    array: {
        min: ({ min }) => `Vähintään ${min}`,
        max: ({ max }) => `Enintään ${max}`,
    },
});

function App() {
    const [language, setLanguage] = useState<Language>('fi');
    const [localization, setLocalization] = useState<Lokalisointi>();
    const [i18n, setI18n] = useState<I18nImpl>(new I18nImpl(defaultLokalisointi, language));

    useEffect(() => {
        async function fetchLocalization() {
            const resp = await axios.get<LokalisointiRivi[]>(
                'https://virkailija.opintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=varda-rekisterointi'
            );
            const lokalisointi: Lokalisointi = resp.data.reduce(
                (acc: Lokalisointi, cur) => ({ ...acc, [cur.locale]: { ...acc[cur.locale], [cur.key]: cur.value } }),
                { fi: {}, sv: {}, en: {} }
            );
            setLocalization(lokalisointi);
            setI18n(new I18nImpl(lokalisointi, language));
        }

        if (!localization) {
            //void fetchLocalization();
        } else {
            setI18n(new I18nImpl(localization, language));
        }
    }, [language, localization]);

    return (
        <>
            <Router>
                <Routes>
                    <Route path="/jotpa" element={<JotpaLanding />} />
                    <Route path="/hakija">
                        <Route path="jotpa/*" element={<JotpaRekisterointi />} />
                    </Route>
                </Routes>
                <Outlet />
            </Router>
            <Footer />
        </>
    );
}

export default App;
