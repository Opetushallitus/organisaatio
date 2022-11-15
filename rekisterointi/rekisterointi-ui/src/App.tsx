import axios from 'axios';
import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Outlet, Route, Routes } from 'react-router-dom';
import { registerLocale } from 'react-datepicker';
import fi from 'date-fns/locale/fi';
import sv from 'date-fns/locale/sv';

// import global styles first
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css';
import 'react-datepicker/dist/react-datepicker.css';
import './styles.css';

import { defaultLokalisointi, I18nImpl, LanguageContext } from './LanguageContext';
import { Language, Lokalisointi } from './types';
import { JotpaRekisterointi } from './jotpa/JotpaRekisterointi';
import { JotpaLanding } from './jotpa/JotpaLanding';
import { JotpaValmis } from './jotpa/JotpaValmis';
import { Footer } from './Footer';

registerLocale('fi', fi);
registerLocale('sv', sv);

function App() {
    const [language, setLanguage] = useState<Language>('fi');
    const [localization, setLocalization] = useState<Lokalisointi>();
    const [i18n, setI18n] = useState<I18nImpl>(new I18nImpl(defaultLokalisointi, language));

    useEffect(() => {
        async function fetchLocalization() {
            const [langResp, localizationResp] = await Promise.all([
                await axios.get<Language>('/api/lokalisointi/kieli'),
                await axios.get<Lokalisointi>('/api/lokalisointi?category=jotpa-rekisterointi'),
            ]);
            setLanguage(langResp.data);
            setLocalization(localizationResp.data);
            setI18n(new I18nImpl(localizationResp.data, language));
        }

        if (!localization) {
            void fetchLocalization();
        } else {
            setI18n(new I18nImpl(localization, language));
        }
    }, [language, localization]);

    if (!localization) {
        return <div></div>;
    }

    return (
        <LanguageContext.Provider value={{ language, setLanguage, i18n }}>
            <a className="skip-to-content" href="#main">
                {i18n.translate('hyppaa_sisaltoon')}
            </a>
            <Router>
                <Routes>
                    <Route path="/jotpa" element={<JotpaLanding />} />
                    <Route path="/jotpa/valmis" element={<JotpaValmis />} />
                    <Route path="/hakija">
                        <Route path="jotpa/*" element={<JotpaRekisterointi />} />
                    </Route>
                </Routes>
                <Outlet />
            </Router>
            <Footer />
        </LanguageContext.Provider>
    );
}

export default App;
