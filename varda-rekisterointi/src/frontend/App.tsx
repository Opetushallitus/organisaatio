import React, { useState } from 'react';
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css'
import Rekisterointi from './hakija/Rekisterointi';
import Rekisteroinnit from "./virkailija/Rekisteroinnit";
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext, I18nImpl } from './contexts';
import { BrowserRouter as Router, Route } from 'react-router-dom';
import { Language, Lokalisointi } from './types';
import useAxios from 'axios-hooks';
import Spinner from './Spinner';

const App: React.FC = () => {
  registerLocale('fi', fi);
  registerLocale('sv', sv);
  registerLocale('en', enGB);
  const [ language, setLanguage ] = useState<Language>('fi');
  const [{ data, loading, error }] = useAxios<Lokalisointi>('/varda-rekisterointi/api/lokalisointi');
  if (loading) {
    return <Spinner />;
  }
  if (error) {
    return <div>error, reload page</div>;
  }
  return (
    <Router basename="/varda-rekisterointi">
      <LanguageContext.Provider value={{ language: language, setLanguage: setLanguage, i18n: new I18nImpl(data[language]) }}>
        <Route path="/hakija" exact component={Rekisterointi} />
        <Route path="/virkailija" exact component={Rekisteroinnit} />
      </LanguageContext.Provider>
    </Router>
  );
}

export default App;
