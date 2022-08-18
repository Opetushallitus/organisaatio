import axios from 'axios';
import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Outlet, Route, Routes } from 'react-router-dom';

// import global styles first
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css';

import { defaultLokalisointi, I18nImpl, LanguageContext } from './contexts';
import { Language, Lokalisointi, LokalisointiRivi } from './types';
import { VardaLanding } from './VardaLanding';

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
            void fetchLocalization();
        } else {
            setI18n(new I18nImpl(localization, language));
        }
    }, [language, localization]);

    if (!Object.keys(i18n._data[language]).length) {
        return <div></div>;
    }

    return (
        <LanguageContext.Provider value={{ language, setLanguage, i18n }}>
            <Router>
                <Routes>
                    <Route path="/" element={<VardaLanding />} />
                </Routes>
                <Outlet />
            </Router>
        </LanguageContext.Provider>
    );
}

export default App;
