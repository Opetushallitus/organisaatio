import React, { useState, useEffect } from 'react';
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css'
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext, I18nImpl, KoodistoImpl, KuntaKoodistoContext } from './contexts';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import {Koodi, Language, Lokalisointi} from './types';
import useAxios from 'axios-hooks';
import Spinner from './Spinner';
import ErrorPage from './virhe/VirheSivu';
import Axios from 'axios';

const OrganisaatioApp: React.FC = () => {
  registerLocale('fi', fi);
  registerLocale('sv', sv);
  registerLocale('en', enGB);
  const [ language, setLanguage ] = useState<Language>('fi');
  const [ languageLoading, setLanguageLoading ] = useState(true);
  useEffect(() => {
    async function fetchLanguage() {
      try {
        const response = await Axios.get('/varda-rekisterointi/api/lokalisointi/kieli');
        setLanguage(response.data);
      } catch (error) {
        console.log(error);
      } finally {
        setLanguageLoading(false);
      }
    }
    fetchLanguage();
  }, []);
  const [{ data: lokalisointi, loading: lokalisointiLoading, error: lokalisointiError }] = useAxios<Lokalisointi>(
      '/varda-rekisterointi/api/lokalisointi');
  const [{ data: kunnat, loading: kunnatLoading, error: kunnatError}] = useAxios<Koodi[]>(
      '/varda-rekisterointi/api/koodisto/KUNTA/koodi?onlyValid=true');
  if (languageLoading || lokalisointiLoading || kunnatLoading) {
    return <Spinner />;
  }
  if (lokalisointiError || kunnatError) {
    return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>
  }
  const i18n = new I18nImpl(lokalisointi, language);
  const kuntaKoodisto = new KoodistoImpl(kunnat, language);
  return (
      <Router basename="/organisaatio-ui">
        <LanguageContext.Provider value={{ language: language, setLanguage: setLanguage, i18n: i18n }}>
          <KuntaKoodistoContext.Provider value={{ koodisto: kuntaKoodisto }}>
            <Switch>
              <Route path="/" exact component={<div>Organisaatioita täällä</div>} />
              <Route path="*">
                <ErrorPage>{i18n.translate('ERROR_404')}</ErrorPage>
              </Route>
            </Switch>
          </KuntaKoodistoContext.Provider>
        </LanguageContext.Provider>
      </Router>
  );
};

export default OrganisaatioApp;
