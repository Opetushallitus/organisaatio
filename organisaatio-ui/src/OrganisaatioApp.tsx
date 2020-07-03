import * as React from 'react';
import {useState, useEffect} from "react";
import { ThemeProvider } from 'styled-components';
import createTheme from '@opetushallitus/virkailija-ui-components/createTheme';
//import 'normalize.css';
// import 'oph-virkailija-style-guide/oph-styles.css'
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import {LanguageContext, I18nImpl, KoodistoImpl, KuntaKoodistoContext} from './contexts/contexts';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import {Koodi, Language, Lokalisointi} from './types/types';
import useAxios from 'axios-hooks';
import Spinner from './components/Spinner/Spinner';
import ErrorPage from './components/Sivut/VirheSivu/VirheSivu';
import Axios from 'axios';
import LomakeSivu from "./components/Sivut/LomakeSivu/LomakeSivu";
import TaulukkoSivu from "./components/Sivut/TaulukkoSivu/TaulukkoSivu";
import Spin from "@opetushallitus/virkailija-ui-components/Spin";

const urlPrefix = process.env.NODE_ENV === 'development' ? '/api' : '';

const theme = createTheme();

const OrganisaatioApp: React.FC = () => {
  registerLocale('fi', fi);
  registerLocale('sv', sv);
  registerLocale('en', enGB);
  const [ language, setLanguage ] = useState<Language>('fi');
  const [ languageLoading, setLanguageLoading ] = useState(true);
  useEffect(() => {
    async function fetchLanguage() {
      try {
        const response = await Axios.get(`${urlPrefix}/lokalisointi/kieli`);
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
      `${urlPrefix}/lokalisointi`);
  const [{ data: kunnat, loading: kunnatLoading, error: kunnatError}] = useAxios<Koodi[]>(
      `${urlPrefix}/koodisto/KUNTA/koodi`);
  if (languageLoading || lokalisointiLoading || kunnatLoading) {
    return (<ThemeProvider theme={theme}>
      <Spin />
    </ThemeProvider>);
  }
  if (lokalisointiError || kunnatError) {
    return <ErrorPage>Tietojen lataaminen epäonnistui. Yritä myöhemmin uudelleen</ErrorPage>
  }
  const i18n = new I18nImpl(lokalisointi, language);
  const kuntaKoodisto = new KoodistoImpl(kunnat, language);
  return (
      <Router basename="/organisaatio-ui">
        <ThemeProvider theme={theme}>
        <LanguageContext.Provider value={{ language: language, setLanguage: setLanguage, i18n: i18n }}>
          <KuntaKoodistoContext.Provider value={{ koodisto: kuntaKoodisto }}>
            <Switch>
              <Route path="/" exact component={TaulukkoSivu} />
              <Route path="/lomake/:oid" component={LomakeSivu} />
              <Route path="*">
                <ErrorPage>{i18n.translate('ERROR_404')}</ErrorPage>
              </Route>
            </Switch>
          </KuntaKoodistoContext.Provider>
        </LanguageContext.Provider>
        </ThemeProvider>
      </Router>
  );
};

export default OrganisaatioApp;
