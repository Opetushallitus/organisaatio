import * as React from 'react';
import * as ReactDOM from 'react-dom';
import './index.css';
import OrganisaatioApp from './OrganisaatioApp';
import axios from 'axios';
import Cookies from 'universal-cookie';
import { ROOT_OID } from './contexts/contexts';

const cookies = new Cookies();
axios.interceptors.request.use((config) => {
    config.headers['Caller-Id'] = `${ROOT_OID}.organisaatio-ui`;
    config.headers['CSRF'] = cookies.get('CSRF');
    return config;
});

ReactDOM.render(
    <React.StrictMode>
        <OrganisaatioApp />
    </React.StrictMode>,
    document.getElementById('root')
);
