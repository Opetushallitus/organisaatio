import React, { useState } from 'react';
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css'
import RekisterointiHakija from './hakija/RekisterointiHakija';
import Rekisteroinnit from "./virkailija/Rekisteroinnit";
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext, I18nImpl, KoodistoImpl, KuntaKoodistoContext } from './contexts';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import {Koodi, Language, Lokalisointi} from './types';
import useAxios from 'axios-hooks';
import Spinner from './Spinner';
import RekisterointiVirkailija from './virkailija/RekisterointiVirkailija';
import ErrorPage from './ErrorPage';
import RekisterointiValmis from './hakija/RekisterointiValmis';
import RekisterointiAloitus from './hakija/RekisterointiAloitus';

const App: React.FC = () => {
  registerLocale('fi', fi);
  registerLocale('sv', sv);
  registerLocale('en', enGB);
  const [ language, setLanguage ] = useState<Language>('fi');
  const [{ data: lokalisointi, loading: lokalisointiLoading, error: lokalisointiError }] = useAxios<Lokalisointi>(
      '/varda-rekisterointi/api/lokalisointi');
  const [{ data: kunnat, loading: kunnatLoading, error: kunnatError}] = useAxios<Koodi[]>(
      '/varda-rekisterointi/api/koodisto/KUNTA/koodi?onlyValid=true');
  if (lokalisointiLoading || kunnatLoading) {
    return <Spinner />;
  }
  if (lokalisointiError || kunnatError) {
    return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>
  }
  const i18n = new I18nImpl(lokalisointi, language);
  const kuntaKoodisto = new KoodistoImpl(kunnat, language);
  return (
    <Router basename="/varda-rekisterointi">
      <LanguageContext.Provider value={{ language: language, setLanguage: setLanguage, i18n: i18n }}>
        <KuntaKoodistoContext.Provider value={{ koodisto: kuntaKoodisto }}>
          <Switch>
            <Route path="/" exact component={RekisterointiAloitus} />
            <Route path="/hakija" exact component={RekisterointiHakija} />
            <Route path="/valmis" exact component={RekisterointiValmis} />
            <Route path="/virkailija" exact component={Rekisteroinnit} />
            <Route path="/virkailija/rekisterointi/luonti/:ytunnus" exact component={RekisterointiVirkailija} />
            <Route path="*">
              <ErrorPage>{i18n.translate('ERROR_404')}</ErrorPage>
            </Route>
          </Switch>
        </KuntaKoodistoContext.Provider>
      </LanguageContext.Provider>
    </Router>
  );
};

export default App;
