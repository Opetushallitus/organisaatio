import React, { useMemo } from 'react';
import { BrowserRouter as Router, Outlet, Route, Routes } from 'react-router-dom';
import { registerLocale } from 'react-datepicker';
import fi from 'date-fns/locale/fi';
import sv from 'date-fns/locale/sv';

// import global styles first
import 'oph-virkailija-style-guide/oph-styles.css';
import 'react-datepicker/dist/react-datepicker.css';
import './styles.css';

import { defaultLokalisointi, I18nImpl, LanguageContext } from './LanguageContext';
import { JotpaRekisterointi } from './jotpa/JotpaRekisterointi';
import { JotpaLanding } from './jotpa/JotpaLanding';
import { JotpaValmis } from './jotpa/JotpaValmis';
import { Footer } from './Footer';
import { useGetLanguageQuery, useGetLocalizationQuery } from './rekisterointiApi';

registerLocale('fi', fi);
registerLocale('sv', sv);

function App() {
    const { data: language } = useGetLanguageQuery();
    const { data: localization } = useGetLocalizationQuery();
    const i18n = useMemo(
        () => new I18nImpl(localization ?? defaultLokalisointi, language ?? 'fi'),
        [language, localization]
    );

    if (!language || !localization) {
        return <div></div>;
    }

    return (
        <LanguageContext.Provider value={{ language, i18n }}>
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
