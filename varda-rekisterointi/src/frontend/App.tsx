import React, { useState } from 'react';
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css'
import RekisterointiHakija from './hakija/RekisterointiHakija';
import Rekisteroinnit from "./virkailija/Rekisteroinnit";
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext, I18nImpl } from './contexts';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import { Language, Lokalisointi } from './types';
import useAxios from 'axios-hooks';
import Spinner from './Spinner';
import RekisterointiVirkailija from './virkailija/RekisterointiVirkailija';
import ErrorPage from './ErrorPage';

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
    return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>
  }
  const i18n = new I18nImpl(data[language]);
  return (
    <Router basename="/varda-rekisterointi">
      <LanguageContext.Provider value={{ language: language, setLanguage: setLanguage, i18n: i18n }}>
        <Switch>
          <Route path="/hakija" exact component={RekisterointiHakija} />
          <Route path="/virkailija" exact component={Rekisteroinnit} />
          <Route path="/virkailija/rekisterointi/luonti/:ytunnus" exact component={RekisterointiVirkailija} />
          <Route path="*">
            <ErrorPage>{i18n.translate('ERROR_404')}</ErrorPage>
          </Route>
        </Switch>
      </LanguageContext.Provider>
    </Router>
  );
}

export default App;
