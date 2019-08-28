import React from 'react';
import 'normalize.css';
import 'oph-virkailija-style-guide/oph-styles.css'
import Rekisterointi from './hakija/Rekisterointi';
import { registerLocale } from 'react-datepicker';
import { fi, sv, enGB } from 'date-fns/locale';
import { LanguageContext } from './contexts';

const App: React.FC = () => {
  registerLocale('fi', fi);
  registerLocale('sv', sv);
  registerLocale('en', enGB);
  return (
    <LanguageContext.Provider value="fi">
      <Rekisterointi />
    </LanguageContext.Provider>
  );
}

export default App;
