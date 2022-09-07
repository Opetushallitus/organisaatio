import React from 'react';
import ReactDOM from 'react-dom/client';
import axios from 'axios';
import Cookies from 'universal-cookie';

import App from './App';

const cookies = new Cookies();

axios.interceptors.request.use((config) => {
    const ophHeaders = {
        'Caller-Id': '1.2.246.562.10.00000000001.varda-rekisterointi',
        CSRF: cookies.get('CSRF'),
    };
    config.headers = { ...config.headers, ...ophHeaders };
    return config;
});

const root = ReactDOM.createRoot(document.getElementById('root')!);
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
