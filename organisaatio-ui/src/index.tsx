import * as React from 'react';
import * as ReactDOM from 'react-dom';
import './index.css';
import axios from 'axios';
import Cookies from 'universal-cookie';
import { ROOT_OID } from './contexts/constants';
import InitializeApp from './components/InitializeApp/InitializeApp';
import OrganisaatioApp from './OrganisaatioApp';

const cookies = new Cookies();
axios.interceptors.request.use((config) => {
    config.headers['Caller-Id'] = `${ROOT_OID}.organisaatio-ui`;
    config.headers['CSRF'] = cookies.get('CSRF');
    return config;
});

ReactDOM.render(
    <React.StrictMode>
        <InitializeApp>
            <OrganisaatioApp />
        </InitializeApp>
    </React.StrictMode>,
    document.getElementById('root')
);
