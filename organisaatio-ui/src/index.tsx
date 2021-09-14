import * as React from 'react';
import * as ReactDOM from 'react-dom';
import './index.css';
import OrganisaatioApp from './OrganisaatioApp';
import axios from 'axios';
import Cookies from 'universal-cookie';
import { ROOT_OID } from './contexts/contexts';
import useFrontProperties from './api/useFrontProperties';
import Loading from './components/Loading/Loading';

const cookies = new Cookies();
axios.interceptors.request.use((config) => {
    config.headers['Caller-Id'] = `${ROOT_OID}.organisaatio-ui`;
    config.headers['CSRF'] = cookies.get('CSRF');
    return config;
});

const InitGate = ({ children }) => {
    const { loading: frontPropertiesLoading, error: frontPropertiesError } = useFrontProperties();
    if (frontPropertiesLoading) {
        return <Loading />;
    } else if (frontPropertiesError) {
        return <div>Sovelluksen lataaminen epäonnistui!</div>;
    } else {
        return children;
    }
};
ReactDOM.render(
    <React.StrictMode>
        <InitGate>
            <OrganisaatioApp />
        </InitGate>
    </React.StrictMode>,
    document.getElementById('root')
);
