import React from 'react';
import ReactDOM from 'react-dom/client';
import axios from 'axios';
import Cookies from 'universal-cookie';

import App from './App';

const cookies = new Cookies();

axios.interceptors.request.use((config) => {
    config.headers.set('Caller-Id', '1.2.246.562.10.00000000001.varda-rekisterointi', true);
    config.headers.set('CSRF', cookies.get('CSRF'), true);
    return config
});

const root = ReactDOM.createRoot(document.getElementById('root')!);
root.render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
