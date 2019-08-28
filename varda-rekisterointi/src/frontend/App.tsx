import React from 'react';
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css'
import Rekisterointi from './hakija/Rekisterointi';
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext } from './contexts';
import { BrowserRouter as Router, Route } from 'react-router-dom';

const App: React.FC = () => {
  registerLocale('fi', fi);
  registerLocale('sv', sv);
  registerLocale('en', enGB);
  return (
    <Router basename="/varda-rekisterointi">
      <LanguageContext.Provider value="fi">
        <Route path="/hakija" exact component={Rekisterointi} />
      </LanguageContext.Provider>
    </Router>
  );
}

export default App;
