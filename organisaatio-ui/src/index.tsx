import * as React from 'react';
import * as ReactDOM from 'react-dom';
import './index.css';
import OrganisaatioApp from './OrganisaatioApp';
import axios from 'axios';
import Cookies from 'universal-cookie';

const cookies = new Cookies();

axios.interceptors.request.use(config => {
    config.headers['Caller-Id'] = '1.2.246.562.10.00000000001.organisaatio-ui';
    config.headers['CSRF'] = cookies.get('CSRF');
    return config;
});


ReactDOM.render(
  <React.StrictMode>
    <OrganisaatioApp />
  </React.StrictMode>,
  document.getElementById('root')
);

